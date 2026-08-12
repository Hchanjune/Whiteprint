package org.whiteprint.platform.infra.persistence.jpa.query

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.whiteprint.platform.core.projection.model.query.cursor.CursorDirection
import java.sql.DriverManager

/**
 * JPA 커서/오프셋 페이지네이션 실측 검증.
 *
 * 확인 대상 두 가지:
 *  1. [ENTITY_ID_ATTRIBUTE] — `@Id`가 `BaseEntity._id` **필드**에 붙어 있을 때 Sort/Specification이 보는
 *     속성명이 정말 `_id`인지(공개 게터 `id`는 `@Transient`라 안 보여야 한다). 코드 리딩만으로 정한 값이라
 *     한 번도 실행된 적이 없었다.
 *  2. tie-breaker — 정렬키에 동점이 있을 때 페이지 간 중복/누락이 없는지.
 *     mongo에서 실제로 났던 버그(primary DESC + FORWARD 동점 그룹)와 같은 형태를 재현한다.
 *
 * 로컬 Postgres에 전용 DB(whiteprint_jpa_cursor_test)를 만들어 돈다. 접속 실패 시 skip.
 * 접속 override: JPA_CURSOR_HOST / JPA_CURSOR_PORT / JPA_CURSOR_USER / JPA_CURSOR_PASSWORD
 */
@EnabledIf("postgresAvailable")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpaCursorSearchVerificationTest @Autowired constructor(
    private val repository: CursorTestItemRepository,
) {

    companion object {
        private val host = System.getenv("JPA_CURSOR_HOST") ?: "192.168.0.12"
        private val port = System.getenv("JPA_CURSOR_PORT") ?: "5432"
        private val user = System.getenv("JPA_CURSOR_USER") ?: "postgres"
        private val password = System.getenv("JPA_CURSOR_PASSWORD") ?: "postgres"
        private const val TEST_DB = "whiteprint_jpa_cursor_test"

        private val adminUrl = "jdbc:postgresql://$host:$port/postgres"
        private val testUrl = "jdbc:postgresql://$host:$port/$TEST_DB"

        /** ExecutionCondition은 스프링 컨텍스트 생성보다 먼저 평가되므로, 여기서 DB까지 만들어둔다. */
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

    /**
     * score DESC, id ASC 로 정렬하면 id 1..10 순서가 된다 — 기대 순서를 읽기 쉽게 만든 배치다.
     * score 20이 3건, 10이 4건, 5가 2건으로 동점 그룹이 페이지 경계에 걸치게 되어 있다(size=3).
     */
    private val seed = listOf(
        1L to 30L,
        2L to 20L, 3L to 20L, 4L to 20L,
        5L to 10L, 6L to 10L, 7L to 10L, 8L to 10L,
        9L to 5L, 10L to 5L,
    )

    @BeforeEach
    fun seedItems() {
        repository.deleteAll()
        repository.saveAll(
            seed.map { (id, score) ->
                CursorTestItem(score = score, label = "item-$id").apply { assignId(id) }
            },
        )
    }

    private fun searchFrom(params: CursorTestParams) =
        repository.cursorSearch(
            params = params,
            idOf = { it.id.toString() },
            idParser = { it.toLong() },
            sortValueOf = { it.score.toString() },
            mapper = { it.id },
        )

    @Test
    @DisplayName("식별자 정렬 속성명: 백킹 필드(_id)와 JPA 식별자 별칭(id)이 모두 같은 결과를 낸다")
    fun identifierAttributeResolves() {
        val byBackingField = repository.findAll(PageRequest.of(0, 5, Sort.by(ENTITY_ID_ATTRIBUTE))).content
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), byBackingField.map { it.id })

        // Hibernate는 "id"를 식별자 별칭으로도 해석한다 — 속성명이 _id여도 통한다.
        val byIdentifierAlias = repository.findAll(PageRequest.of(0, 5, Sort.by("id"))).content
        assertEquals(byBackingField.map { it.id }, byIdentifierAlias.map { it.id })
    }

    @Test
    @DisplayName("커서 순회는 동점 정렬키에서도 모든 행을 정확히 한 번씩 돌려준다")
    fun cursorTraversalCoversEveryRowExactlyOnce() {
        val collected = mutableListOf<Long>()
        var params = CursorTestParams(size = 3)
        var pages = 0

        while (true) {
            val page = searchFrom(params)
            assertEquals(seed.size.toLong(), page.totalCount, "totalCount는 커서 경계와 무관해야 한다")
            collected += page.content
            if (!page.hasNextPage) break
            params = params.copy(cursor = page.endCursor)
            check(++pages < 20) { "커서가 진행하지 않는다 — 무한 루프" }
        }

        assertEquals(seed.size, collected.size, "중복 또는 누락")
        assertEquals(seed.size, collected.toSet().size, "중복된 행이 있다")
        assertEquals((1L..10L).toList(), collected, "score DESC, id ASC 순서가 아니다")
    }

    @Test
    @DisplayName("오프셋 페이지네이션도 tie-breaker 덕에 페이지가 겹치지 않는다")
    fun offsetPagesDoNotOverlap() {
        val collected = (1..4).flatMap { pageNumber ->
            repository
                .findAll(OffsetTestParams(page = pageNumber, size = 3).toPageable())
                .content
                .map { it.id }
        }

        assertEquals((1L..10L).toList(), collected)
    }

    @Test
    @DisplayName("BACKWARD는 깊은 페이지에서도 직전 페이지를 돌려준다 (첫 페이지로 점프하지 않음)")
    fun backwardFromDeeperPageReturnsThePrecedingPage() {
        val first = searchFrom(CursorTestParams(size = 3))
        val second = searchFrom(CursorTestParams(size = 3, cursor = first.endCursor))
        val third = searchFrom(CursorTestParams(size = 3, cursor = second.endCursor))
        assertEquals(listOf(1L, 2L, 3L), first.content)
        assertEquals(listOf(4L, 5L, 6L), second.content)
        assertEquals(listOf(7L, 8L, 9L), third.content)

        val back = searchFrom(
            CursorTestParams(size = 3, cursor = third.startCursor, direction = CursorDirection.BACKWARD),
        )

        // 정렬을 뒤집어 조회한 뒤 되뒤집으므로 "직전 size개"가 논리 순서로 나온다.
        assertEquals(second.content, back.content)
        assertTrue(back.hasPreviousPage, "1페이지가 더 앞에 남아 있다")
        assertTrue(back.hasNextPage, "커서를 들고 왔으므로 뒤쪽에도 페이지가 있다")
    }

    @Test
    @DisplayName("BACKWARD로 처음까지 되돌아가면 hasPreviousPage가 꺼진다")
    fun backwardStopsAtTheBeginning() {
        val first = searchFrom(CursorTestParams(size = 3))
        val second = searchFrom(CursorTestParams(size = 3, cursor = first.endCursor))

        val back = searchFrom(
            CursorTestParams(size = 3, cursor = second.startCursor, direction = CursorDirection.BACKWARD),
        )

        assertEquals(first.content, back.content)
        assertFalse(back.hasPreviousPage, "더 앞에 남은 페이지가 없다")
    }

    @Test
    @DisplayName("변조된 커서는 INVALID_CURSOR로 걸러진다")
    fun tamperedCursorIsRejected() {
        assertThrows(Exception::class.java) {
            searchFrom(CursorTestParams(cursor = "not-a-valid-cursor"))
        }
    }

    @Test
    @DisplayName("필터가 걸려도 totalCount는 필터 기준으로 센다")
    fun totalCountFollowsFilterOnly() {
        val filtered = repository.cursorSearch(
            params = CursorTestParams(size = 2),
            filter = { root, _, cb -> cb.greaterThan(root.get("score"), 5L) },
            idOf = { it.id.toString() },
            idParser = { it.toLong() },
            sortValueOf = { it.score.toString() },
            mapper = { it.id },
        )

        assertEquals(8L, filtered.totalCount)
        assertEquals(listOf(1L, 2L), filtered.content)
        assertTrue(filtered.hasNextPage)
    }
}
