package org.whiteprint.platform.infra.persistence.jpa.query

import jakarta.persistence.criteria.Path

/**
 * `.`으로 끊어 연관 경로를 순회한다 — `"author.nickname"` -> `root.get("author").get("nickname")`.
 * 점이 없으면 단일 `get`과 동일하므로 기존 호출과 호환된다.
 *
 * mongo 실행기는 `$lookup`으로 만든 계산 필드를 `sortBy.field`에 그대로 쓸 수 있는데, JPA에는 그에 대응하는
 * `preSortStages`가 없다. 조인 대상 컬럼을 가리키는 수단이 이 점 표기다.
 *
 * 주의: 여기서 만들어지는 경로는 **암묵적 inner join**이다. 연관이 없는 행은 결과에서 빠지므로,
 * outer join이 필요하면 호출부가 `Specification` 안에서 `root.join(..., JoinType.LEFT)`로 직접 조립해야 한다.
 */
@Suppress("UNCHECKED_CAST")
fun <T> Path<*>.resolvePath(attributePath: String): Path<T> =
    attributePath
        .split('.')
        .fold(this) { path, segment -> path.get<Any>(segment) } as Path<T>
