package org.whiteprint.platform.adapter.cache.servlet.support

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.whiteprint.platform.core.cache.model.CachedOperation
import org.whiteprint.platform.infra.serializer.jackson.JacksonRedisSerializers

/**
 * 유스케이스 캐싱의 **회귀 가드**.
 *
 * `OperationResult` 는 `data` 만 [CachedOperation] 봉투에 담아 저장한다. 그 봉투의 `data` 는 `Any` 라
 * 타입 정보가 없으면 꺼낼 때 `LinkedHashMap` 이 되고, 호출부는 캐스팅에서 터진다.
 * 그리고 그 실패는 **첫 요청이 아니라 두 번째 요청부터** 나타난다 — 배포 검증을 통과하고 운영에서 터지는
 * 종류라 여기로 당겨온다. Redis 는 필요 없다(문제는 네트워크가 아니라 직렬화기다).
 */
class CachedOperationSerializationTest {

    private val serializer = JacksonRedisSerializers.json()

    data class ProjectionLike(
        val id: String,
        val count: Int,
        val lastSeen: String?,
    )

    @Test
    fun `봉투에 담긴 data 가 구체 타입 그대로 복원된다`() {
        val original = CachedOperation(ProjectionLike(id = "1", count = 7, lastSeen = null))

        val restored = serializer.deserialize(serializer.serialize(original))

        assertInstanceOf(CachedOperation::class.java, restored)
        assertEquals(original, restored)
        assertInstanceOf(ProjectionLike::class.java, (restored as CachedOperation).data)
    }

    @Test
    fun `봉투가 아닌 값은 그대로 왕복한다`() {
        val original = ProjectionLike(id = "2", count = 0, lastSeen = "x")

        assertEquals(original, serializer.deserialize(serializer.serialize(original)))
    }

}
