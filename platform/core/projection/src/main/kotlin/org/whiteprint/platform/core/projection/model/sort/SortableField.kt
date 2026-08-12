package org.whiteprint.platform.core.projection.model.sort

/**
 * 사용자가 선택 가능한 정렬 필드의 화이트리스트 계약. 각 query 서비스가 enum으로 구현한다.
 *
 * ```kotlin
 * enum class ScenarioSortField(
 *     override val field: String,
 *     override val valueType: SortValueType,
 * ): SortableField {
 *     INSERTED_AT("inserted_at", SortValueType.INSTANT),
 *     LIKE_COUNT("like_count", SortValueType.LONG),   // $lookup 계산 필드도 가능
 * }
 * ```
 *
 * - 웹 Params가 이 enum 타입으로 `sortBy`를 바인딩하면 화이트리스트 검증은 바인딩 단계에서 끝난다.
 * - [valueType] 덕분에 커서 경계값 파싱이 자동화된다 — 저장소마다 타입 분기 코드를 쓸 필요 없음.
 */
interface SortableField {
    /** enum 상수명. 클라이언트가 보내는 파라미터 값이자 응답 meta에 에코되는 값. (enum이면 자동 제공) */
    val name: String

    /**
     * 저장소가 이해하는 정렬 대상의 이름. **의미가 저장소마다 다르므로 enum 하나는 저장소 하나에 귀속된다.**
     *
     * - mongo: raw 저장 필드명(snake_case). `$lookup`으로 만든 aggregation 계산 필드명도 허용.
     * - JPA: **엔티티 속성명**(컬럼명 아님). 연관 경로는 `"author.nickname"` 점 표기.
     *
     * 같은 enum을 두 저장소에 재사용하면 한쪽이 조용히 0건이 되거나(mongo에 속성명) 예외가 난다(JPA에 컬럼명).
     */
    val field: String

    /** 커서 경계값의 타입. */
    val valueType: SortValueType

    /**
     * 클라이언트 wire name (kebab-case). 기본: INSERTED_AT -> "inserted-at". 필요 시 override.
     * 웹 바인딩(SortableFieldConverterFactory)과 응답 meta 에코 양쪽에 쓰인다.
     */
    val paramName: String get() = name.lowercase().replace('_', '-')
}
