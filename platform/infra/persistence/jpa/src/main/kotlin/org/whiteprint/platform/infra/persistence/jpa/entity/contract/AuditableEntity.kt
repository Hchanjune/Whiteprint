package org.whiteprint.platform.infra.persistence.jpa.entity.contract

interface AuditableEntity: org.whiteprint.platform.infra.persistence.jpa.entity.contract.InsertableEntity,
    org.whiteprint.platform.infra.persistence.jpa.entity.contract.UpdatableEntity,
    org.whiteprint.platform.infra.persistence.jpa.entity.contract.DeletableEntity