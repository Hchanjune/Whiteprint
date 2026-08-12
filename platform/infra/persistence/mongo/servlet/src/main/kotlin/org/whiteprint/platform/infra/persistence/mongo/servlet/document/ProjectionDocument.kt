package org.whiteprint.platform.infra.persistence.mongo.servlet.document

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.whiteprint.platform.core.projection.model.projection.Projection

/**
 * servlet 스택의 읽기측 문서. 범용 [MongoDocument]와 필드는 같고, **식별자를 String으로 못박는다**.
 *
 * 이 제약이 필요한 이유는 커서 페이지네이션이다 — 커서는 `"sortValue|id"`를 Base64로 인코딩한
 * 문자열 토큰이라, 경계 조건이 `Criteria.where("_id").gt(문자열)`로 조립된다.
 * `_id`가 Long이나 ObjectId인 컬렉션에 문자열로 비교를 걸면 Mongo의 타입 브래키팅 때문에
 * **에러 없이 0건**이 나온다. 타입으로 막지 않으면 조용히 깨지는 경로가 된다.
 *
 * 읽기측이 아닌 범용 mongo 저장소는 [MongoDocument]/[SoftDeletableMongoDocument]를 그대로 쓴다.
 */
abstract class ProjectionDocument : MongoDocument<String>(), Projection {

    @get:Id
    abstract override val id: String

    /** 읽기 모델은 이벤트 재반영/복구 대상이므로 하드 삭제하지 않는 것이 기본이다. */
    @get:Transient
    override val useSoftDelete: Boolean get() = true

}
