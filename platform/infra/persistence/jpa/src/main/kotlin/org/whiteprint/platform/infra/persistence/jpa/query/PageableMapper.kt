package org.whiteprint.platform.infra.persistence.jpa.query

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.whiteprint.platform.core.projection.model.query.offset.OffsetQueryParams
import org.whiteprint.platform.core.projection.model.sort.SortDirection
import org.springframework.data.domain.Sort as SpringSort

/**
 * BaseEntity의 식별자 **속성명**. `@Id`가 `private var _id` 필드에 붙어 있어 Hibernate가 필드 접근으로
 * 매핑하므로, JPQL/Sort가 보는 이름은 `id`(@Transient 게터)가 아니라 `_id`다.
 * 식별자를 다르게 매핑한 엔티티는 [toPageable]에 속성명을 직접 넘긴다.
 */
const val ENTITY_ID_ATTRIBUTE: String = "_id"

/**
 * OffsetQueryParams.page는 1-based, Spring Data Pageable은 0-based라 여기서 한 번 변환한다.
 *
 * 정렬에는 [tieBreakerAttribute]를 항상 뒤에 붙인다. tie-breaker가 없으면 정렬키에 동점이 있을 때
 * DB가 동점 그룹의 순서를 보장하지 않아, 페이지를 넘길 때 같은 행이 두 페이지에 겹쳐 나오거나 누락된다.
 * (mongo 커서/오프셋의 `_id` tie-breaker와 같은 이유이고, 저장소 간 정렬 결과를 일치시키는 역할도 한다)
 *
 * 주의: [OffsetQueryParams.sortBy]의 `field`는 mongo에서는 raw 저장 필드명이지만 여기서는
 * **엔티티 속성명**이어야 한다 — JPA `Sort`는 컬럼명이 아니라 메타모델 속성명으로 해석하므로
 * snake_case 컬럼명을 넣으면 기동/조회 시점에 예외가 난다.
 */
fun OffsetQueryParams.toPageable(tieBreakerAttribute: String = ENTITY_ID_ATTRIBUTE): Pageable =
    PageRequest.of(
        page - 1,
        size,
        SpringSort
            .by(
                if (sortDirection == SortDirection.ASC) SpringSort.Direction.ASC else SpringSort.Direction.DESC,
                sortBy.field,
            )
            .and(SpringSort.by(SpringSort.Direction.ASC, tieBreakerAttribute)),
    )
