package org.whiteprint.platform.infra.persistence.mongo.reactive.query

import org.springframework.data.mongodb.core.query.Criteria
import org.whiteprint.platform.core.projection.model.search.MatchMode
import org.whiteprint.platform.core.projection.model.search.SearchKeyword
import java.util.regex.Pattern

/** Pattern.quote로 정규식 메타문자를 이스케이프한 뒤 앵커(^, $)를 바깥에서 붙인다. */
fun SearchKeyword.toCriteria(field: String): Criteria {
    val quoted = Pattern.quote(value)
    return when (mode) {
        MatchMode.EQUALS -> Criteria.where(field).`is`(value)
        MatchMode.CONTAINS -> Criteria.where(field).regex(quoted)
        MatchMode.STARTS_WITH -> Criteria.where(field).regex("^$quoted")
        MatchMode.ENDS_WITH -> Criteria.where(field).regex("$quoted$")
    }
}
