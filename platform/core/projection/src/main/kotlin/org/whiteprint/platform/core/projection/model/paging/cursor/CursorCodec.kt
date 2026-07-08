package org.whiteprint.platform.core.projection.model.paging.cursor

import org.whiteprint.platform.core.projection.policy.QueryException
import org.whiteprint.platform.core.projection.policy.QueryPolicy
import java.util.Base64

/**
 * opaque cursor 인코딩: "sortValue|id"를 Base64Url로 감싸 클라이언트에겐 불투명한 토큰으로 노출한다.
 * sortValue 안에 구분자가 들어갈 수 있으므로 마지막 등장 위치를 기준으로 분리한다.
 * 정렬 기준 1개 + id tie-breaker 조합만 표현 가능 — 복합 정렬 커서는 미지원(추후 확장 포인트).
 */
object CursorCodec {
    private const val DELIMITER = "|"

    fun encode(cursor: Cursor): String {
        val raw = "${cursor.sortValue}$DELIMITER${cursor.id}"
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toByteArray(Charsets.UTF_8))
    }

    /** 변조되었거나 다른 형식(과거 버전 등)으로 만들어진 토큰이 들어오면 raw 파싱 예외 대신 QueryException으로 감싼다. */
    fun decode(token: String): Cursor {
        val raw = try {
            String(Base64.getUrlDecoder().decode(token), Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            throw QueryException(QueryPolicy.INVALID_CURSOR, mapOf("key" to "cursor", "value" to token), cause = e)
        }

        val delimiterIndex = raw.lastIndexOf(DELIMITER)
        if (delimiterIndex < 0) {
            throw QueryException(QueryPolicy.INVALID_CURSOR, mapOf("key" to "cursor", "value" to token))
        }

        return Cursor(
            sortValue = raw.substring(0, delimiterIndex),
            id = raw.substring(delimiterIndex + 1),
        )
    }
}
