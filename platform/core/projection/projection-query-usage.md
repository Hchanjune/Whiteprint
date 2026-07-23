# Projection Query 사용 가이드 (계약 / 정렬 / 커서 검색 / DSL)

query 사이드(조회) 기능을 만들 때의 실용 가이드. 대상 모듈: `core:projection`,
`infra:persistence:mongo:reactive`, `adapter:web:{reactive,servlet}`.

## 1. 계층 계약

command 사이드(Request→Command)와 대칭:

```
Params(QueryParams) --toQuery(principal, pathVar)--> Query --Service--> ReadModel --toViewModel--> ViewModel
      adapter.in.web                              port.in            application         adapter.in.web
```

| 마커 | 위치 | 역할 | 규칙 |
|---|---|---|---|
| `QueryParams` | adapter.in.web | 조회 요청 바인딩 모델 | 모든 조회 엔드포인트가 하나를 받는다(없으면 빈 객체). adapter 밖 금지 |
| `Query` | application port.in | 조회 입력 | `toQuery()`로만 생성(principal/pathVar 합성). 응답 직렬화 금지 |
| `ReadModel` | application | service 반환 계약 | 단일 `Projection` 또는 조합 모델. ViewModel 반환 금지(의존 역전) |
| `Projection` | application | 영속 형상 read model | `: ReadModel`. 메타필드 6종(id/version/insertedAt/updatedAt/isDeleted/deletedAt) |
| `ViewModel` | adapter.in.web | 최종 응답 | 웹 매퍼에서 ReadModel→ViewModel 변환 |

- `CursorQueryParams`/`OffsetQueryParams`는 **페이지네이션 능력 인터페이스**로,
  QueryParams/Query 마커를 상속하지 않는다. 웹 Params 와 Query 가 **각각** 구현한다
  (한 타입이 두 계층을 관통하는 것을 차단).
- 응답에는 Query 객체가 아니라 `toMeta()` 스냅샷(`CursorPageMeta`/`OffsetPageMeta`)만 실린다.

## 2. 정렬 (SortableField)

### enum 작성 — 필드당 한 줄이 곧 화이트리스트

```kotlin
enum class ArticleSortField(
    override val field: String,          // ⚠️ raw 저장 필드명 (snake_case). aggregation 계산 필드도 가능
    override val valueType: SortValueType, // 커서 경계값 파싱 타입: STRING/LONG/DOUBLE/INSTANT
    val fromStats: Boolean = false,      // (앱 자유 확장) $lookup 필요 여부 등
): SortableField {
    INSERTED_AT("inserted_at", SortValueType.INSTANT),
    LIKE_COUNT("like_count", SortValueType.LONG, fromStats = true),
}
```

- `paramName`(wire name)은 자동 파생: `INSERTED_AT` → `"inserted-at"`. 필요 시 override.
- **웹 바인딩은 canonical 비교**(영숫자만+lowercase): `inserted-at`/`inserted_at`/`insertedAt`/`INSERTED_AT`
  전부 허용, 미등록 값은 400. `sortDirection=desc`, `direction=forward` 소문자도 허용.
  (adapter:web AutoConfiguration 이 컨버터를 자동 등록 — 앱 설정 불필요)
- 응답 meta 에코는 kebab 고정: `"sortBy": "inserted-at"`.
- **fallback 정렬 = Params 의 기본값**: `override val sortBy: ArticleSortField = INSERTED_AT`.
- ⚠️ 정렬 허용 필드엔 **인덱스**가 있는지 확인할 것.

## 3. 검색 만들기 레시피

### ① Params (adapter.in.web)

```kotlin
data class SearchArticleParams(
    val title: String? = null,                       // 검색어 (와일드카드: *foo*, foo*, *foo)
    override val cursor: String? = null,
    override val direction: CursorDirection = CursorDirection.FORWARD,
    override val size: Int = 20,
    override val sortBy: ArticleSortField = ArticleSortField.INSERTED_AT,  // = fallback
    override val sortDirection: SortDirection = SortDirection.DESC,
): QueryParams, CursorQueryParams
```

### ② Query (port.in) + toQuery (웹 매퍼)

```kotlin
data class SearchArticleQuery(
    val accountId: String?,                          // principal 은 toQuery 에서 합성
    val title: SearchKeyword? = null,
    override val cursor: String?, /* direction/size/sortBy/sortDirection ... */
): Query, CursorQueryParams

fun SearchArticleParams.toQuery(accountId: String?) = SearchArticleQuery(
    accountId = accountId,
    title = title?.let(SearchKeyword::parse),        // *와일드카드* → MatchMode 파싱
    ...
)
```

### ③ Repository — cursorSearch + criteria DSL

```kotlin
override suspend fun search(query: SearchArticleQuery, includeDeleted: Boolean) =
    mongoOperations.cursorSearch(
        params = query,
        entityClass = ArticleDocument::class.java,   // 컬렉션명 + converter 매핑 자동
        filter = buildCriteria(query, includeDeleted),
        preSortStages = statsSortStages(query),      // $lookup 필요 시에만
    ) { it.toProjection() }

private fun buildCriteria(query: SearchArticleQuery, includeDeleted: Boolean): Criteria = criteria {
    notDeleted(unless = includeDeleted)
    orGroup {                                        // 그룹 내부는 OR
        eq("_id", query.articleIdEq)
        keyword("title", query.title)                // SearchKeyword MatchMode 반영
    }
    eq("genre", query.genreEq)
    range("inserted_at", query.insertedFrom, query.insertedTo)
}
```

컨벤션: **필터는 `private fun buildCriteria(...)` 로**, lookup 스테이지는 `private fun xxxSortStages(...)` 로.

### ④ Service / ⑤ Controller

```kotlin
// service: ReadModel 반환, mapContent 로 조합
override suspend fun search(query: SearchArticleQuery): OperationResult<CursorPagedData<ArticleSearchedItem>> =
    ReactiveOperations { repository.search(query).mapContent { ArticleSearchedItem(it, ...) } }

// controller: 웹 매퍼로 ViewModel 변환 (mapContent 는 totalCount/meta 자동 보존)
return ResponseEntityGenerator.generateFromOperation(operation) { operation.data.toViewModel() }
```

## 4. 응답 형태

```json
{
  "content": [ ...ViewModel ],
  "totalCount": 35,            // 검색조건 적용 총계 (커서 경계 제외 → 페이지 이동에도 유지)
  "size": 20,
  "hasNextPage": true, "hasPreviousPage": false,
  "startCursor": "…", "endCursor": "…",
  "meta": { "cursor": null, "size": 20, "direction": "FORWARD", "sortBy": "inserted-at", "sortDirection": "DESC" }
}
```

- 커서는 opaque(Base64Url `"sortValue|id"`). **발급 당시의 sortBy 와 짝** — 정렬을 바꾸면
  커서를 버리고 첫 페이지부터 (불일치 시 `QUERY_INVALID_CURSOR` 400).
- totalCount 는 매 페이지 count 쿼리 1회 비용(페이지 조회와 병렬 실행).

## 5. cursorSearch 레퍼런스 (mongo reactive)

```kotlin
// 타입 매핑 버전 (일반용)
suspend fun <D, T> ReactiveMongoOperations.cursorSearch(
    params: CursorQueryParams, entityClass: Class<D>,
    filter: Criteria? = null, preSortStages: List<AggregationOperation> = emptyList(),
    mapper: (D) -> T,
): CursorPagedData<T>

// raw Document 버전 (컬렉션명 직접 지정, 매핑 직접 제어)
suspend fun <T> ReactiveMongoOperations.cursorSearch(
    params: CursorQueryParams, collectionName: String, ..., mapper: (Document) -> T,
): CursorPagedData<T>
```

파이프라인: `match(filter) -> preSortStages -> match(커서 경계) -> sort -> limit(size+1)` +
count(filter) 병렬 + 커서 인코딩(idOf/sortValueOf 자동, BSON Date→ISO 변환).

### $lookup 정렬 패턴 (다른 컬렉션 필드로 정렬)

```kotlin
private fun statsSortStages(query: SearchArticleQuery): List<AggregationOperation> {
    if (!query.sortBy.fromStats) return emptyList()
    return listOf(
        Aggregation.lookup("article_stats", "_id", "article_id", "stats"),
        Aggregation.addFields()
            .addField(query.sortBy.field)   // 예: "like_count" — enum 의 field 가 계산 필드명
            .withValue(ConditionalOperators.ifNull(
                ArrayOperators.ArrayElemAt.arrayOf("stats.${query.sortBy.field}").elementAt(0)
            ).then(0L))
            .build(),
    )
}
```

### 제약/함정

- ⚠️ **untyped aggregation — filter/preSortStages 의 필드명은 전부 raw(저장 필드명)**.
  프로퍼티명(`insertedAt`)은 매핑되지 않고 그대로 몽고에 전달되어 **에러 없이 조용히 무시**된다.
  (find/count 의 typed 경로는 프로퍼티명도 매핑되지만, 헷갈리면 raw 로 통일하라)
- count 는 filter 만 본다 — **filter 가 preSortStages 의 계산 필드에 의존하면 totalCount 가 틀린다**
  (그 경우 pipeline count 로 확장 필요).
- 정렬은 **단일 필드 + `_id` ASC tie-breaker** 만 지원. 커서 경계의 tie-break 비교는
  primary 방향이 아니라 **탐색 방향**(FORWARD=`_id gt`)을 따른다 — 동점 그룹 중복/누락 버그의
  원인이었던 지점(0.6.7 에서 수정).

## 6. criteria DSL 레퍼런스

null 값은 자동 스킵(옵셔널 필터의 null 체크 관용구 제거). 최상위는 AND, 빈 블록 = 전체 매치.

| 함수 | 의미 |
|---|---|
| `eq` / `ne` / `gt` / `gte` / `lt` / `lte` | 비교 (value null 이면 스킵) |
| `isIn(field, values)` | `$in` (null/빈 컬렉션 스킵) |
| `keyword(field, searchKeyword)` | `SearchKeyword` MatchMode 반영 — EQUALS(`is`) / CONTAINS / STARTS_WITH / ENDS_WITH (regex, Pattern.quote 이스케이프) |
| `range(field, from, to)` | 폐구간 gte/lte. 한쪽만 있어도 동작, 둘 다 null 이면 스킵 |
| `orGroup { ... }` | 블록 내부를 OR 로 묶음. 전부 스킵되어 비면 그룹째 스킵 |
| `custom(criteria)` | DSL 미표현 파생 필터의 escape hatch (예: `containsRomance` → `romance_rate gt 0`) |
| `notDeleted(unless = includeDeleted)` | soft delete 관례 필터(`is_deleted=false`). unless=true 면 스킵 |

### SearchKeyword 와일드카드 (클라이언트 문법)

| 입력 | MatchMode |
|---|---|
| `판타지` | EQUALS |
| `*판타지*` | CONTAINS |
| `판타지*` | STARTS_WITH |
| `*판타지` | ENDS_WITH |
| `*`, `**` | 파싱 결과 null → 필터 없음 |

## 7. 에러 정책 (QueryPolicy 주요)

| 상황 | 정책 | HTTP |
|---|---|---|
| 단건 조회 결과 없음 | `QUERY_NOT_FOUND` | 404 |
| 커서 변조/타입 불일치(정렬 변경 후 재사용 포함) | `QUERY_INVALID_CURSOR` | 400 |
| 미등록 sortBy / 잘못된 enum 값 | (바인딩 실패) | 400 |
