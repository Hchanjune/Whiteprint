package org.whiteprint.platform.infra.persistence.jpa.query

import org.springframework.data.jpa.domain.Specification
import org.whiteprint.platform.core.projection.model.search.MatchMode
import org.whiteprint.platform.core.projection.model.search.SearchKeyword

private const val LIKE_ESCAPE_CHAR = '\\'

/** value 안에 리터럴로 들어있는 LIKE 와일드카드(%, _)를 이스케이프해서 조립한다. */
private fun escapeLike(value: String): String =
    value
        .replace("$LIKE_ESCAPE_CHAR", "$LIKE_ESCAPE_CHAR$LIKE_ESCAPE_CHAR")
        .replace("%", "$LIKE_ESCAPE_CHAR%")
        .replace("_", "${LIKE_ESCAPE_CHAR}_")

fun <T: Any> SearchKeyword.toSpecification(field: String): Specification<T> =
    Specification { root, _, cb ->
        val path = root.get<String>(field)
        when (mode) {
            MatchMode.EQUALS -> cb.equal(path, value)
            MatchMode.CONTAINS -> cb.like(path, "%${escapeLike(value)}%", LIKE_ESCAPE_CHAR)
            MatchMode.STARTS_WITH -> cb.like(path, "${escapeLike(value)}%", LIKE_ESCAPE_CHAR)
            MatchMode.ENDS_WITH -> cb.like(path, "%${escapeLike(value)}", LIKE_ESCAPE_CHAR)
        }
    }
