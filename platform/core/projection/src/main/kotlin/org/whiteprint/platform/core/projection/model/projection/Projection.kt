package org.whiteprint.platform.core.projection.model.projection

import org.whiteprint.platform.core.projection.model.readModel.ReadModel
import java.time.Instant

/**
 * 영속 형상의 읽기 모델 계약. 이벤트를 반영해 만들어지는 query 사이드의 기본 단위.
 *
 * - [version]: 원본(command 사이드) 애그리거트의 버전. upsert 시 stale 반영을 걸러내는 가드 키로 쓰인다.
 * - soft delete 여부는 저장 어댑터의 관심사이므로 이 계약에 포함하지 않는다.
 *   (mongo reactive의 ProjectionDocument 등 인프라 계층에서 결정)
 */
interface Projection : ReadModel {
    val id: String
    val version: Long
    val insertedAt: Instant
    val updatedAt: Instant
    val isDeleted: Boolean
    val deletedAt: Instant?
}
