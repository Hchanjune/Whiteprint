package org.whiteprint.platform.core.kernel.policy.exception

import org.whiteprint.platform.core.kernel.policy.Policy

abstract class StandardException(
    val policy: Policy,
    val attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
): RuntimeException(
    interpolate(policy.message, attributes, cause),
    cause
) {

    val status: Int = policy.status
    val code: String = policy.code

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