package org.whiteprint.platform.infra.persistence.mongo.servlet.document

import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Field
import java.io.Serializable
import java.time.Instant

abstract class MongoDocument<ID : Serializable> {

    abstract val id: ID
    abstract val version: Long

    @get:Field("inserted_at")
    abstract val insertedAt: Instant

    @get:Field("updated_at")
    abstract val updatedAt: Instant

    @get:Field("is_deleted")
    abstract val isDeleted: Boolean

    @get:Field("deleted_at")
    abstract val deletedAt: Instant?

    /**
     * soft delete 여부는 저장 어댑터의 정책이지 문서의 상태가 아니다 —
     * `@Transient`가 없으면 이 값이 문서 본문에 같이 저장되어, 정책이 코드와 DB 두 군데에 존재하게 된다.
     *
     * 오버라이드는 반드시 `get() =` computed property로 한다. 프로퍼티 이니셜라이저(`= true`)로 재정의하면
     * 상위 생성자가 끝난 뒤에야 값이 채워져, 생성 도중에 읽는 경로에서 상위 기본값이 보인다.
     */
    @get:Transient
    open val useSoftDelete: Boolean get() = false

}
