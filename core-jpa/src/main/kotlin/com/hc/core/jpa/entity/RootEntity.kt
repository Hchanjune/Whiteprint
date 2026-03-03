package com.hc.core.jpa.entity

import com.hc.core.domain.entity.Identifiable
import com.hc.core.domain.identifier.TsidGenerator
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PreUpdate
import jakarta.persistence.Version
import java.time.Instant

@MappedSuperclass
abstract class RootEntity: Identifiable<Long>, AuditableEntity, VersionedEntity {

    @Id
    @Column(name = "id")
    override val id: Long = TsidGenerator.generate()
    @Column(name = "inserted_at", nullable = false)
    override var insertedAt: Instant = Instant.now()
        protected set
    @Column(name = "updated_at", nullable = false)
    override var updatedAt: Instant = Instant.now()
        protected set
    @Column(name = "is_deleted", nullable = false)
    override var isDeleted: Boolean = false
        protected set
    @Column(name = "deleted_at", nullable = true)
    override var deletedAt: Instant? = null
        protected set
    @Version
    @Column(name = "version", nullable = false)
    override var version: Long = 0
        protected set

    @PreUpdate
    override fun preUpdate() {
        updatedAt = Instant.now()
    }

    override fun delete() {
        isDeleted = true
        deletedAt = Instant.now()
    }

    override fun restore() {
        isDeleted = false
    }

}