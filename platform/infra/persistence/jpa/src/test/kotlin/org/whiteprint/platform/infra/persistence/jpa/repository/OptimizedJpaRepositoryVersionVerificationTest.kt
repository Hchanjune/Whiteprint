package org.whiteprint.platform.infra.persistence.jpa.repository

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.whiteprint.platform.infra.persistence.jpa.entity.withId
import java.sql.DriverManager

/**
 * `@Version` 이 **항상 오르고, 그 증가가 호출부에 즉시 보이는지** 실측한다.
 *
 * 이 두 가지가 깨지면 프로젝션 이벤트가 소비처의 `version` 가드(`lt`)에 걸려
 * 예외도 로그도 없이 사라진다 — 코드 리딩으로는 절대 안 보이는 실패라 실제 DB 로 돌린다.
 *
 * 로컬 Postgres 에 전용 DB(whiteprint_jpa_version_test)를 만들어 돈다. 접속 실패 시 skip.
 * 접속 override: JPA_VERSION_HOST / JPA_VERSION_PORT / JPA_VERSION_USER / JPA_VERSION_PASSWORD
 */
@EnabledIf("postgresAvailable")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class OptimizedJpaRepositoryVersionVerificationTest @Autowired constructor(
    private val softRepository: SoftDeletableItemRepository,
    private val hardRepository: HardDeletableItemRepository,
    private val entityManager: EntityManager,
) {

    companion object {
        private val host = System.getenv("JPA_VERSION_HOST") ?: "192.168.0.12"
        private val port = System.getenv("JPA_VERSION_PORT") ?: "5432"
        private val user = System.getenv("JPA_VERSION_USER") ?: "postgres"
        private val password = System.getenv("JPA_VERSION_PASSWORD") ?: "postgres"
        private const val TEST_DB = "whiteprint_jpa_version_test"

        private val adminUrl = "jdbc:postgresql://$host:$port/postgres"
        private val testUrl = "jdbc:postgresql://$host:$port/$TEST_DB"

        @JvmStatic
        fun postgresAvailable(): Boolean = try {
            DriverManager.getConnection(adminUrl, user, password).use { connection ->
                connection.createStatement().use { statement ->
                    val exists = statement
                        .executeQuery("SELECT 1 FROM pg_database WHERE datname = '$TEST_DB'")
                        .use { it.next() }
                    if (!exists) statement.executeUpdate("CREATE DATABASE $TEST_DB")
                }
            }
            true
        } catch (e: Exception) {
            false
        }

        @JvmStatic
        @DynamicPropertySource
        fun datasource(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { testUrl }
            registry.add("spring.datasource.username") { user }
            registry.add("spring.datasource.password") { password }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        }
    }

    private var nextId = 1L
    private fun newId(): Long = nextId++

    @Test
    @DisplayName("값이 하나도 안 바뀐 저장도 version 을 올린다 (touch 자동화)")
    fun noOpSaveStillIncrementsVersion() {
        val id = newId()
        softRepository.save(SoftDeletableItem(label = "same").withId(id))
        entityManager.flush()
        val created = softRepository.findById(id).orElseThrow().version

        // 어떤 필드도 건드리지 않고 다시 저장한다 — 예전이면 UPDATE 자체가 생략되던 경로다.
        softRepository.save(softRepository.findById(id).orElseThrow())
        entityManager.flush()
        val afterNoOp = softRepository.findById(id).orElseThrow().version

        assertTrue(afterNoOp > created, "no-op 저장인데 version 이 그대로다: $created -> $afterNoOp")
    }

    @Test
    @DisplayName("soft delete 는 isDeleted/deletedAt 을 세우고 version 증가까지 즉시 보이게 한다")
    fun softDeleteFlushesVersion() {
        val id = newId()
        softRepository.save(SoftDeletableItem(label = "x").withId(id))
        entityManager.flush()

        val target = softRepository.findById(id).orElseThrow()
        val before = target.version

        softRepository.delete(target)

        // delete() 안에서 flush 까지 하므로 여기서 수동 flush 없이 값이 보여야 한다.
        assertTrue(target.isDeleted, "soft delete 인데 isDeleted 가 서지 않았다")
        assertNotNull(target.deletedAt, "soft delete 인데 deletedAt 이 비어 있다")
        assertTrue(target.version > before, "삭제 후 version 이 그대로다: $before -> ${target.version}")
    }

    @Test
    @DisplayName("생성-해제-재구독-재해제 전 구간에서 version 이 단조 증가한다")
    fun versionIsStrictlyMonotonicAcrossLifecycle() {
        val id = newId()
        softRepository.save(SoftDeletableItem(label = "lifecycle").withId(id))
        entityManager.flush()

        val versions = mutableListOf<Long>()
        versions += softRepository.findById(id).orElseThrow().version

        softRepository.delete(softRepository.findById(id).orElseThrow())
        versions += softRepository.findById(id).orElseThrow().version

        softRepository.restore(softRepository.findById(id).orElseThrow())
        versions += softRepository.findById(id).orElseThrow().version

        softRepository.delete(softRepository.findById(id).orElseThrow())
        versions += softRepository.findById(id).orElseThrow().version

        // 소비처 가드가 `stored < incoming` 이라, 같은 값이 두 번 나오면 뒤쪽이 조용히 버려진다.
        assertEquals(versions.sorted(), versions, "version 이 단조 증가하지 않는다: $versions")
        assertEquals(versions.size, versions.distinct().size, "version 이 중복된다: $versions")
    }

    @Test
    @DisplayName("restore 는 삭제 표식을 되돌린다")
    fun restoreClearsDeletionMarkers() {
        val id = newId()
        softRepository.save(SoftDeletableItem(label = "back").withId(id))
        entityManager.flush()
        softRepository.delete(softRepository.findById(id).orElseThrow())

        val revived = softRepository.findById(id).orElseThrow()
        softRepository.restore(revived)

        assertFalse(revived.isDeleted, "restore 했는데 isDeleted 가 남아 있다")
        assertNull(revived.deletedAt, "restore 했는데 deletedAt 이 남아 있다")
    }

    @Test
    @DisplayName("다건 soft delete 도 전부 삭제 표식이 서고 version 이 오른다")
    fun deleteAllSoftDeletesEveryRow() {
        val ids = List(3) { newId() }
        ids.forEach { softRepository.save(SoftDeletableItem(label = "bulk-$it").withId(it)) }
        entityManager.flush()

        val targets = ids.map { softRepository.findById(it).orElseThrow() }
        val before = targets.associate { it.id to it.version }

        softRepository.deleteAll(targets)

        targets.forEach { item ->
            assertTrue(item.isDeleted, "다건 삭제인데 id=${item.id} 의 isDeleted 가 서지 않았다")
            assertTrue(
                item.version > before.getValue(item.id),
                "다건 삭제인데 id=${item.id} 의 version 이 그대로다",
            )
        }
    }

    @Test
    @DisplayName("물리 삭제 엔티티는 여전히 행이 사라진다 (soft delete 로 바뀌지 않는다)")
    fun hardDeleteStillRemovesTheRow() {
        val id = newId()
        hardRepository.save(HardDeletableItem(label = "gone").withId(id))
        entityManager.flush()

        hardRepository.delete(hardRepository.findById(id).orElseThrow())
        entityManager.flush()
        entityManager.clear()

        assertTrue(hardRepository.findById(id).isEmpty, "물리 삭제인데 행이 남아 있다")
    }
}
