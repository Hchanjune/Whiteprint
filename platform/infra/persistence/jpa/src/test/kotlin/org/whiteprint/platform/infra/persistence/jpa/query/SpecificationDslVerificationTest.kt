package org.whiteprint.platform.infra.persistence.jpa.query

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.whiteprint.platform.core.projection.model.search.SearchKeyword
import java.sql.DriverManager

/**
 * `specification { }` DSL 실측 검증 — mongo `criteria { }`의 JPA 대응물.
 *
 * 검증 축:
 *  1. null 자동 스킵 / 빈 블록 전체 매치
 *  2. [SearchKeyword] 와일드카드 4종이 LIKE로 정확히 옮겨지는지 (이스케이프 포함)
 *  3. **orGroup 함정** — 전체를 감싸면 필터가 넓어져 삭제 행이 노출되는 것을 재현
 *  4. 점 표기 경로 / 속성명 오타가 조용한 0건이 아니라 예외인지
 *
 * 로컬 Postgres 필요, 접속 실패 시 skip. 접속 override는 [JpaCursorSearchVerificationTest]와 동일.
 */
@EnabledIf("postgresAvailable")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpecificationDslVerificationTest @Autowired constructor(
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

    /** label에 LIKE 메타문자(`%`, `_`)를 일부러 섞어 이스케이프를 시험한다. */
    private val seed = listOf(
        Triple(1L, "alpha", false),
        Triple(2L, "alphabet", false),
        Triple(3L, "beta", false),
        Triple(4L, "gamma", false),
        Triple(5L, "50% off", false),
        Triple(6L, "snake_case", false),
        Triple(7L, "deleted-alpha", true),
    )

    @BeforeEach
    fun seedItems() {
        repository.deleteAll()
        repository.saveAll(
            seed.map { (id, label, deleted) ->
                CursorTestItem(score = id, label = label).apply {
                    assignId(id)
                    if (deleted) delete()
                }
            },
        )
    }

    private fun idsOf(specification: org.springframework.data.jpa.domain.Specification<CursorTestItem>) =
        repository.findAll(specification).map { it.id }.sorted()

    @Test
    @DisplayName("null 값은 자동 스킵되고 빈 블록은 전체 매치다")
    fun nullsAreSkippedAndEmptyBlockMatchesAll() {
        assertEquals(seed.map { it.first }, idsOf(specification { }))

        val allNull = specification<CursorTestItem> {
            eq("label", null)
            range("score", null, null)
            keyword("label", null)
            isIn("score", emptyList<Long>())
            custom(null)
        }
        assertEquals(seed.map { it.first }, idsOf(allNull))
    }

    @Test
    @DisplayName("SearchKeyword 와일드카드 4종이 LIKE로 옮겨지고 메타문자는 이스케이프된다")
    fun keywordModesTranslateToLike() {
        assertEquals(listOf(1L), idsOf(specification { keyword("label", SearchKeyword.parse("alpha")) }))
        assertEquals(listOf(1L, 2L), idsOf(specification { keyword("label", SearchKeyword.parse("alpha*")) }))
        assertEquals(listOf(1L, 7L), idsOf(specification { keyword("label", SearchKeyword.parse("*alpha")) }))
        assertEquals(listOf(1L, 2L, 7L), idsOf(specification { keyword("label", SearchKeyword.parse("*alpha*")) }))

        // 리터럴 %, _ 가 와일드카드로 새지 않아야 한다 — 새면 전체 행이 매치된다.
        assertEquals(listOf(5L), idsOf(specification { keyword("label", SearchKeyword.parse("*50%*")) }))
        assertEquals(listOf(6L), idsOf(specification { keyword("label", SearchKeyword.parse("*e_c*")) }))
    }

    @Test
    @DisplayName("notDeleted는 소프트 삭제 행을 제외한다 (속성명 isDeleted)")
    fun notDeletedExcludesSoftDeletedRows() {
        val alive = specification<CursorTestItem> { notDeleted() }
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L), idsOf(alive))

        val includingDeleted = specification<CursorTestItem> { notDeleted(unless = true) }
        assertEquals(seed.map { it.first }, idsOf(includingDeleted))
    }

    @Test
    @DisplayName("★ orGroup으로 전체를 감싸면 필터가 넓어져 삭제 행이 노출된다 (mongo와 동일한 함정)")
    fun orGroupWideningTrap() {
        // 올바른 조립: 검색어끼리만 OR, 삭제 필터는 최상위 AND
        val correct = specification<CursorTestItem> {
            notDeleted()
            orGroup {
                keyword("label", SearchKeyword.parse("*alpha*"))
                eq("score", 3L)
            }
        }
        assertEquals(listOf(1L, 2L, 3L), idsOf(correct))

        // 잘못된 조립: notDeleted까지 OR 안에 넣으면 삭제 행(7)이 딸려 나온다
        val widened = specification<CursorTestItem> {
            orGroup {
                notDeleted()
                keyword("label", SearchKeyword.parse("*alpha*"))
                eq("score", 3L)
            }
        }
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L), idsOf(widened))
    }

    @Test
    @DisplayName("범위/집합/비교 연산이 최상위 AND로 묶인다")
    fun rangeAndSetPredicatesAreAnded() {
        val spec = specification<CursorTestItem> {
            notDeleted()
            range("score", 2L, 5L)
            ne("label", "gamma")
        }
        assertEquals(listOf(2L, 3L, 5L), idsOf(spec))

        assertEquals(listOf(1L, 4L), idsOf(specification { isIn("score", listOf(1L, 4L)) }))
        assertEquals(listOf(6L, 7L), idsOf(specification { gt("score", 5L) }))
    }

    @Test
    @DisplayName("속성명이 틀리면 조용한 0건이 아니라 예외다 (mongo와 반대)")
    fun unknownAttributeThrowsInsteadOfMatchingNothing() {
        assertThrows(Exception::class.java) {
            idsOf(specification { eq("is_deleted", false) })  // raw 컬럼명 — mongo 습관
        }
    }
}
