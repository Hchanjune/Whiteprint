package org.whiteprint.platform.infra.persistence.jpa.entity.contract

/** 나열 순서가 곧 구현체의 멤버 생성 순서다 — `version, insertedAt, updatedAt, isDeleted, deletedAt`. */
interface AuditableEntity: VersionableEntity, InsertableEntity, UpdatableEntity, DeletableEntity
