package com.hc.infra.jpa.core.entity.contract

interface AuditableEntity: com.hc.infra.jpa.core.entity.contract.InsertableEntity,
    com.hc.infra.jpa.core.entity.contract.UpdatableEntity, com.hc.infra.jpa.core.entity.contract.DeletableEntity