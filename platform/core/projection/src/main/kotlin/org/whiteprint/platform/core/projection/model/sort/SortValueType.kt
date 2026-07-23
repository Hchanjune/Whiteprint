package org.whiteprint.platform.core.projection.model.sort

import org.whiteprint.platform.core.projection.policy.QueryException
import org.whiteprint.platform.core.projection.policy.QueryPolicy
import java.time.Instant

/**
 * 정렬 필드 값의 타입. 커서에 문자열로 실려 온 경계값을 실제 타입으로 복원할 때 쓴다.
 * (커서 인코딩은 `value.toString()` — Instant는 ISO-8601, 숫자는 10진 문자열)
 */
enum class SortValueType {
    STRING {
        override fun parseRaw(raw: String): Any = raw
    },
    LONG {
        override fun parseRaw(raw: String): Any = raw.toLong()
    },
    DOUBLE {
        override fun parseRaw(raw: String): Any = raw.toDouble()
    },
    INSTANT {
        override fun parseRaw(raw: String): Any = Instant.parse(raw)
    },
    ;

    protected abstract fun parseRaw(raw: String): Any

    /** 파싱 실패는 변조되었거나 다른 정렬 필드로 만들어진 커서 → INVALID_CURSOR로 감싼다. */
    fun parse(raw: String): Any = try {
        parseRaw(raw)
    } catch (e: Exception) {
        throw QueryException(QueryPolicy.INVALID_CURSOR, mapOf("key" to "sortValue", "value" to raw), cause = e)
    }
}
