package org.whiteprint.platform.infra.persistence.jpa.repository

import org.whiteprint.platform.infra.persistence.jpa.entity.BaseEntity
import org.whiteprint.platform.infra.persistence.jpa.entity.contract.DeletableEntity
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.transaction.annotation.Transactional
import java.io.Serializable

class OptimizedJpaRepository<T: Any, ID: Serializable>(
    entityInformation: JpaEntityInformation<T, ID>,
    private val entityManager: EntityManager
): org.springframework.data.jpa.repository.support.SimpleJpaRepository<T, ID>(entityInformation, entityManager) {

    @Transactional
    override fun <S: T> save(entity: S): S {
        return if (entity is org.whiteprint.platform.infra.persistence.jpa.entity.BaseEntity<out Serializable>) {
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

    @Transactional
    override fun delete(entity: T) {
        if (entity is org.whiteprint.platform.infra.persistence.jpa.entity.contract.DeletableEntity) {
            if (entity.useSoftDelete) {
                entity.delete()
                this.save(entity)
                return
            }
        }
        super.delete(entity)
    }

    @Transactional
    override fun deleteById(id: ID) {
        _root_ide_package_.org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById(id).ifPresent { this.delete(it) }
    }

}