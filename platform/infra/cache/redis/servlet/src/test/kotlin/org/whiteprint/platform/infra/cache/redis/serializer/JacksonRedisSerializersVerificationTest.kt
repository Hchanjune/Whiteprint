package org.whiteprint.platform.infra.cache.redis.serializer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * 캐시 직렬화기의 **회귀 가드**.
 *
 * 여기가 깨지면 나타나는 증상이 고약하다 — 캐시에 넣는 것은 성공하고
 * **두 번째 조회부터** 실패해서, 첫 배포 검증은 통과하고 운영에서 터진다.
 * 그래서 실패를 여기로 당겨온다. Redis 는 필요 없다(문제는 네트워크가 아니라 매퍼다).
 */
class JacksonRedisSerializersVerificationTest {

    private val serializer = JacksonRedisSerializers.json()

    /** 주 생성자만 있고 no-arg 생성자가 없는 형태 — KotlinModule 없이는 복원이 안 된다. */
    data class CachedValue(
        val count: Int,
        val lastId: String?,
    )

    @Test
    fun `Kotlin data class 는 왕복해도 값이 보존된다`() {
        val original = CachedValue(count = 7, lastId = "123456789")

        val restored = serializer.deserialize(serializer.serialize(original))

        assertEquals(original, restored)
    }

    @Test
    fun `nullable 필드가 null 이어도 왕복한다`() {
        val original = CachedValue(count = 0, lastId = null)

        val restored = serializer.deserialize(serializer.serialize(original))

        assertEquals(original, restored)
    }

    /**
     * default typing 이 살아 있어야 구체 타입으로 복원된다.
     * 끄면 조용히 `LinkedHashMap` 이 돌아와서, 캐시가 "동작은 하는데 값이 이상한" 상태가 된다.
     */
    @Test
    fun `타입 정보가 유지되어 구체 타입으로 복원된다`() {
        val bytes = serializer.serialize(CachedValue(1, "x"))!!

        assertInstanceOf(CachedValue::class.java, serializer.deserialize(bytes))
        assertTrue(String(bytes).contains("@class")) { "type hint 가 사라졌다: ${String(bytes)}" }
    }

}
