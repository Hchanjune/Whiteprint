package com.hc.infra.persistence.jpa.entity.contract

interface AuditableEntity: InsertableEntity,
    UpdatableEntity, DeletableEntity