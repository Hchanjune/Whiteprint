package org.whiteprint.platform.core.kernel.policy.exception

import io.github.hchanjune.omk.core.metric.FaultAttributable
import org.whiteprint.platform.core.kernel.policy.Policy

abstract class StandardException(
    val policy: Policy,
    val attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): RuntimeException(
    interpolate(policy.message, attributes, cause),
    cause
), FaultAttributable {

    val status: Int = policy.status
    val code: String = policy.code

    /**
     * OMK 스팬 결과 판정용 귀책 — 정책 status 가 4xx 면 클라이언트 귀책(FAILURE_CLIENT), 그 외는 서버(FAILURE_SERVER).
     * 없으면 OMK 가 예외 타입만 보고 정책 예외(없음·잔액 부족 등)를 전부 FAILURE_SERVER 로 집계한다.
     */
    override val isClientFault: Boolean
        get() = status in 400..499

    companion object {
        private val ATTRIBUTE_PATTERN = Regex("\\[\\[(.*?)]]")

        private const val MISSING_VALUE_MARKER = "N/A"

        private fun interpolate(template: String, attributes: Map<String, Any>, cause: Throwable?): String {
            cause?.printStackTrace()
            if (!template.contains("[[")) return template
            return ATTRIBUTE_PATTERN.replace(template) { match ->
                val key = match.groupValues[1].trim()
                attributes[key]?.toString() ?: MISSING_VALUE_MARKER
            }
        }
    }

}