package org.whiteprint.platform.infra.persistence.jpa.query

import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.whiteprint.platform.core.projection.model.paging.cursor.CursorPagedData
import org.whiteprint.platform.core.projection.model.paging.cursor.toCursorPagedData
import org.whiteprint.platform.core.projection.model.query.cursor.CursorQueryParams

/** 조건 없음(전체 매치). `Specification.unrestricted()` 대신 두어 Spring Data 버전 차이에 영향받지 않게 한다. */
private fun <E : Any> unrestricted(): Specification<E> = Specification { _, _, cb -> cb.conjunction() }

/**
 * 커서 검색 실행기(JPA) — 커서 경계 술어/정렬/limit(size+1)/totalCount count/커서 인코딩을 전부 처리한다.
 * 호출부에 남는 것은 필터 [Specification]과 값 추출 람다뿐.
 *
 * mongo 실행기와 다른 점:
 * - **`preSortStages`가 없다.** 조인은 [filter] 안에서 표현하고, 정렬/경계가 조인 컬럼을 가리켜야 하면
 *   `sortBy.field`에 점 표기를 쓴다([resolvePath]).
 * - **`idOf`/`idParser`/`sortValueOf`를 호출부가 준다.** mongo는 raw `Document`에서 실행기가 직접 꺼낼 수
 *   있었지만 여기서는 엔티티가 타입이 있어 리플렉션 없이 꺼내려면 호출부가 아는 편이 낫다.
 *   특히 [idParser]는 생략할 수 없다 — [BaseEntity]가 `ID : Serializable` 제네릭이라 메타모델이 알려주는
 *   식별자 타입이 지운 타입(`java.io.Serializable`)이라서 실행기가 스스로 복원할 방법이 없다(실측 확인).
 * - **count와 page 조회가 순차 실행된다.** JPA/Hibernate 세션은 스레드에 묶여 있어 병렬화 대상이 아니다.
 *
 * 주의:
 * - totalCount는 [filter]만 적용해 센다(커서 경계 제외 — 페이지 이동에도 값 유지).
 * - [filter]에 조인이 걸려 있으면 count 쿼리에도 같은 조인이 나간다. 중복 행이 생기는 조인이라면
 *   호출부에서 `distinct` 처리를 해야 totalCount가 맞는다.
 */
fun <E : Any, T> JpaSpecificationExecutor<E>.cursorSearch(
    params: CursorQueryParams,
    idOf: (E) -> String,
    idParser: (String) -> Comparable<*>,
    sortValueOf: (E) -> String,
    filter: Specification<E>? = null,
    tieBreakerAttribute: String = ENTITY_ID_ATTRIBUTE,
    mapper: (E) -> T,
): CursorPagedData<T> {
    val filterSpec = filter ?: unrestricted()
    val totalCount = count(filterSpec)

    val pageSpec = params.cursorBoundarySpecification<E>(idParser, tieBreakerAttribute)
        ?.let(filterSpec::and)
        ?: filterSpec

    // findBy는 <S : E, R>이라 두 타입 모두 추론되지 않는다 — S=E(프로젝션 없음), R=List<E>로 명시한다.
    val fetched = findBy<E, List<E>>(pageSpec) { query ->
        query
            .sortBy(params.toSpringSort(tieBreakerAttribute))
            .limit(params.size + 1)
            .all()
    }

    return fetched.toCursorPagedData(
        params = params,
        totalCount = totalCount,
        idOf = idOf,
        sortValueOf = sortValueOf,
        mapper = mapper,
    )
}
