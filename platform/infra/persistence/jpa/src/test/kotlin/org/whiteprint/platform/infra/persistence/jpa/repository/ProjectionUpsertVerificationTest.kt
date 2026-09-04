package org.whiteprint.platform.infra.persistence.jpa.repository

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
import java.sql.DriverManager
import java.time.Instant

/**
 * 프로젝션 upsert 의 **stale 가드가 실제로 도는지** 실측한다.
 *
 * 이 가드가 깨지면 늦게 온 옛 이벤트가 새 상태를 덮거나, 반대로 새 이벤트가 조용히 버려진다 —
 * 둘 다 예외도 로그도 없어서 코드 리딩으로는 보이지 않는다. 그래서 실제 DB 로 돌린다.
 *
 * 로컬 Postgres 에 전용 DB(whiteprint_jpa_version_test)를 만들어 돈다. 접속 실패 시 skip.
 */
@EnabledIf("postgresAvailable")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class ProjectionUpsertVerificationTest @Autowired constructor(
    private val singleKeyRepository: SingleKeyProjectionRepository,
    private val compositeKeyRepository: CompositeKeyProjectionRepository,
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

    private fun single(id: Long, version: Long, label: String, deleted: Boolean = false) =
        SingleKeyProjectionEntity(id = id, label = label).apply {
            this.version = version
            this.insertedAt = Instant.parse("2026-01-01T00:00:00Z")
            this.updatedAt = Instant.parse("2026-01-02T00:00:00Z")
            this.isDeleted = deleted
            this.deletedAt = if (deleted) Instant.parse("2026-01-02T00:00:00Z") else null
        }

    private fun reload(id: Long): SingleKeyProjectionEntity {
        entityManager.flush()
        entityManager.clear()
        return singleKeyRepository.findById(id).orElseThrow()
    }

    @Test
    @DisplayName("처음 오는 이벤트는 삽입된다")
    fun insertsOnFirstEvent() {
        val id = newId()
        assertTrue(singleKeyRepository.upsertProjection(single(id, version = 0, label = "first")))
        assertEquals("first", reload(id).label)
    }

    @Test
    @DisplayName("version 이 더 큰 이벤트는 반영된다")
    fun appliesNewerVersion() {
        val id = newId()
        singleKeyRepository.upsertProjection(single(id, version = 3, label = "old"))
        assertTrue(singleKeyRepository.upsertProjection(single(id, version = 4, label = "new")))

        val stored = reload(id)
        assertEquals("new", stored.label)
        assertEquals(4, stored.version)
    }

    @Test
    @DisplayName("version 이 같거나 낮은 이벤트는 무시된다 (stale 가드)")
    fun ignoresStaleVersion() {
        val id = newId()
        singleKeyRepository.upsertProjection(single(id, version = 5, label = "current"))

        assertFalse(
            singleKeyRepository.upsertProjection(single(id, version = 5, label = "same-version")),
            "같은 version 인데 반영됐다 — 가드가 `<` 가 아니라 `<=` 로 동작한다",
        )
        assertFalse(
            singleKeyRepository.upsertProjection(single(id, version = 4, label = "older")),
            "낮은 version 인데 반영됐다",
        )

        val stored = reload(id)
        assertEquals("current", stored.label, "stale 이벤트가 값을 덮었다")
        assertEquals(5, stored.version)
    }

    @Test
    @DisplayName("해제 이벤트는 is_deleted/deleted_at 을 그대로 받아 적는다")
    fun appliesDeletionAsPlainState() {
        val id = newId()
        singleKeyRepository.upsertProjection(single(id, version = 1, label = "alive"))
        singleKeyRepository.upsertProjection(single(id, version = 2, label = "alive", deleted = true))

        val stored = reload(id)
        assertTrue(stored.isDeleted)
        assertEquals(Instant.parse("2026-01-02T00:00:00Z"), stored.deletedAt)

        // 되살아나는 것도 이벤트 하나로 표현된다 — 복제본이 자체 restore 를 돌리지 않는다.
        singleKeyRepository.upsertProjection(single(id, version = 3, label = "alive"))
        assertFalse(reload(id).isDeleted)
    }

    @Test
    @DisplayName("복합 키: id 가 같아도 source_type 이 다르면 서로 다른 행이다")
    fun compositeKeyKeepsRowsApart() {
        val sharedId = newId()

        fun of(sourceType: String, version: Long, label: String) =
            CompositeKeyProjectionEntity(sourceType = sourceType, id = sharedId, label = label).apply {
                this.version = version
                this.insertedAt = Instant.parse("2026-01-01T00:00:00Z")
                this.updatedAt = Instant.parse("2026-01-01T00:00:00Z")
            }

        assertTrue(compositeKeyRepository.upsertProjection(of("SCENARIO", 0, "scenario-side")))
        assertTrue(
            compositeKeyRepository.upsertProjection(of("EPISODE", 0, "episode-side")),
            "id 가 같다고 다른 소유 도메인의 행을 덮었다 — 복합 키가 동작하지 않는다",
        )

        entityManager.flush()
        entityManager.clear()
        assertEquals(
            "scenario-side",
            compositeKeyRepository.findById(CompositeKeyProjectionEntity.Key("SCENARIO", sharedId))
                .orElseThrow().label,
        )
        assertEquals(
            "episode-side",
            compositeKeyRepository.findById(CompositeKeyProjectionEntity.Key("EPISODE", sharedId))
                .orElseThrow().label,
        )
    }

    @Test
    @DisplayName("복합 키에서도 stale 가드는 같은 행에만 적용된다")
    fun compositeKeyGuardIsPerRow() {
        val sharedId = newId()

        fun of(sourceType: String, version: Long, label: String) =
            CompositeKeyProjectionEntity(sourceType = sourceType, id = sharedId, label = label).apply {
                this.version = version
                this.insertedAt = Instant.parse("2026-01-01T00:00:00Z")
                this.updatedAt = Instant.parse("2026-01-01T00:00:00Z")
            }

        compositeKeyRepository.upsertProjection(of("SCENARIO", 9, "scenario-current"))

        // 다른 소유 도메인의 낮은 version 은 저쪽 행의 가드와 무관하다.
        assertTrue(compositeKeyRepository.upsertProjection(of("EPISODE", 1, "episode-new")))
        assertFalse(compositeKeyRepository.upsertProjection(of("SCENARIO", 1, "scenario-stale")))

        entityManager.flush()
        entityManager.clear()
        assertEquals(
            "scenario-current",
            compositeKeyRepository.findById(CompositeKeyProjectionEntity.Key("SCENARIO", sharedId))
                .orElseThrow().label,
        )
    }
}
