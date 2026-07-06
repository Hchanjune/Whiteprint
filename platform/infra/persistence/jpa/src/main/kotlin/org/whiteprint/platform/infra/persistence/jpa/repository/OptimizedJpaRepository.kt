package org.whiteprint.platform.infra.persistence.jpa.repository

import org.whiteprint.platform.infra.persistence.jpa.entity.BaseEntity
import org.whiteprint.platform.infra.persistence.jpa.entity.contract.DeletableEntity
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import java.io.Serializable

@Suppress("SpringTransactionalMethodCallsInspection")
class OptimizedJpaRepository<T: Any, ID: Serializable>(
    entityInformation: JpaEntityInformation<T, ID>,
    private val entityManager: EntityManager
): SimpleJpaRepository<T, ID>(entityInformation, entityManager) {

    override fun <S: T> save(entity: S): S {
        return if (entity is BaseEntity<out Serializable>) {
            if (entity.isNew) {
                entityManager.persist(entity)
                entity
            } else {
                entityManager.merge(entity)
            }
        } else {
            super.save(entity)
        }
    }

    override fun delete(entity: T) {
        if (entity is DeletableEntity) {
            if (entity.useSoftDelete) {
                entity.delete()
                this.save(entity)
                return
            }
        }
        super.delete(entity)
    }

    override fun deleteById(id: ID) {
        findById(id).ifPresent { this.delete(it) }
    }

    fun restore(entity: T) {
        if (entity is DeletableEntity && entity.useSoftDelete) {
            entity.restore()
            this.save(entity)
        }
    }

    fun restoreById(id: ID) {
        findById(id).ifPresent { this.restore(it) }
    }

}