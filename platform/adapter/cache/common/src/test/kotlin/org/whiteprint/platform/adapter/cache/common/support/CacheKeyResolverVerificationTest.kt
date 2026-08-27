package org.whiteprint.platform.adapter.cache.common.support

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.whiteprint.platform.core.cache.annotation.CacheEvictKey
import org.whiteprint.platform.core.cache.annotation.CachedKey

/**
 * 키 형식과 이름 해석의 **계약 고정**.
 *
 * 여기서 지키는 건 셋이다:
 * - 선언 순서가 키에 영향을 주지 않는다 (이름순 정렬)
 * - 조각이 하나여도 `이름=값` 형식이 같다 (조각이 늘어도 기존 표현이 안 바뀐다)
 * - 이름을 비우면 필드/파라미터 이름을 쓴다
 */
class CacheKeyResolverVerificationTest {

    @Test
    fun `키 조각은 선언 순서가 아니라 이름순으로 정렬된다`() {
        val declaredInReverse = listOf("zebra" to "1", "apple" to "2")

        assertEquals("apple=2:zebra=1", CacheKeyResolver.buildKeyPart(declaredInReverse))
    }

    @Test
    fun `조각이 하나여도 이름을 붙인 형식을 그대로 쓴다`() {
        assertEquals("recipientId=123", CacheKeyResolver.buildKeyPart(listOf("recipientId" to "123")))
    }

    // --- resolveNames: 기동 검사가 쓰는 정적 경로 ---

    @Suppress("unused")
    private class Holder {
        fun byParameters(
            @CachedKey recipientId: String,
            @CachedKey("scenario") scenarioId: String,
        ) = Unit

        fun byArgumentFields(query: Query) = Unit
    }

    private class Query(
        @field:CachedKey val recipientIdEq: String,
        @field:CacheEvictKey("recipientId") val alias: String,
    )

    @Test
    fun `파라미터 이름을 자동으로 쓰고 명시 이름이 있으면 그걸 쓴다`() {
        val method = Holder::class.java.getDeclaredMethod("byParameters", String::class.java, String::class.java)

        val names = CacheKeyResolver.resolveNames(method, CachedKey::class.java) { it.name }

        // 정렬되므로 선언 순서(recipientId, scenario)와 무관하게 사전순으로 나온다
        assertEquals(listOf("recipientId", "scenario"), names)
    }

    @Test
    fun `파라미터에 없으면 인자 타입의 필드에서 찾는다`() {
        val method = Holder::class.java.getDeclaredMethod("byArgumentFields", Query::class.java)

        assertEquals(
            listOf("recipientIdEq"),
            CacheKeyResolver.resolveNames(method, CachedKey::class.java) { it.name },
        )
        assertEquals(
            listOf("recipientId"),
            CacheKeyResolver.resolveNames(method, CacheEvictKey::class.java) { it.name },
        )
    }

}
