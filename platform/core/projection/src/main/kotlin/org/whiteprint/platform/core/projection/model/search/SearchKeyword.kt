package org.whiteprint.platform.core.projection.model.search

data class SearchKeyword(
    val value: String,
    val mode: MatchMode,
) {
    companion object {
        private const val WILDCARD = "*"

        /** 컨트롤 문자만 남고 값이 비면(예: "*", "**") 검색하지 않은 것으로 간주해 null을 반환한다. */
        fun parse(raw: String): SearchKeyword? {
            val (value, mode) = when {
                raw.startsWith(WILDCARD) && raw.endsWith(WILDCARD) ->
                    raw.removePrefix(WILDCARD).removeSuffix(WILDCARD) to MatchMode.CONTAINS
                raw.startsWith(WILDCARD) ->
                    raw.removePrefix(WILDCARD) to MatchMode.ENDS_WITH
                raw.endsWith(WILDCARD) ->
                    raw.removeSuffix(WILDCARD) to MatchMode.STARTS_WITH
                else ->
                    raw to MatchMode.EQUALS
            }
            return value.takeIf { it.isNotBlank() }?.let { SearchKeyword(it, mode) }
        }
    }
}
