package org.whiteprint.platform.infra.persistence.jpa.repository

import org.whiteprint.platform.infra.persistence.jpa.entity.BaseEntity
import org.whiteprint.platform.infra.persistence.jpa.entity.RootEntity
import org.whiteprint.platform.infra.persistence.jpa.entity.contract.DeletableEntity
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import java.io.Serializable

/**
 * 저장/삭제에서 **`@Version` 이 반드시 오르고, 그 증가가 호출부에 보이도록** 보장한다.
 *
 * 이 보장이 필요한 이유는 프로젝션 계약 때문이다. 쓰기 한 번에 아웃바운드 이벤트 하나가 나가고,
 * 소비처는 `version` 이 저장된 것보다 **커야만**(`lt` 가드) 그 이벤트를 반영한다.
 * version 이 안 오르거나 낡은 값으로 나가면 이벤트는 예외도 로그도 없이 사라진다.
 *
 * 여기서 막지 않으면 어댑터/매퍼 수십 곳이 각자 기억해야 하는 규칙이 된다.
 */
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
                // 값이 하나도 안 바뀐 갱신은 Hibernate 가 UPDATE 자체를 생략한다 —
                // 그러면 @Version 도 @PreUpdate 도 돌지 않아 version 과 updatedAt 이 그대로다.
                // updatedAt 을 건드려 항상 dirty 로 만들어 둔다.
                (entity as? RootEntity<*>)?.touch()
                entityManager.merge(entity)
            }
        } else {
            super.save(entity)
        }
    }

    /**
     * soft delete 대상이면 물리 삭제 대신 `isDeleted`/`deletedAt` 을 세우고 **flush 까지 한다.**
     *
     * flush 가 필요한 이유: @Version 증가는 flush 시점에 일어난다. 여기서 flush 하지 않으면
     * 호출부가 읽는 version 이 **삭제 이전 값**이라, 그 값으로 만든 삭제 이벤트가
     * 뒤따르는 restore 이벤트와 같은 번호가 되어 둘 중 하나가 가드에 걸려 사라진다.
     *
     * ⚠ 증가된 version 이 보이는 것은 **managed 엔티티**일 때다(`findById` 로 얻은 것).
     * detached 엔티티를 넘기면 `merge` 가 별도 사본을 만들므로 넘긴 쪽 객체는 갱신되지 않는다.
     */
    override fun delete(entity: T) {
        if (softDelete(entity)) {
            entityManager.flush()
            return
        }
        super.delete(entity)
    }

    /** 건마다 flush 하지 않고 마지막에 한 번만 한다 — 다건 삭제에서 왕복이 N 배가 되지 않도록. */
    override fun deleteAll(entities: MutableIterable<T>) {
        var flushRequired = false
        entities.forEach { entity ->
            if (softDelete(entity)) flushRequired = true else super.delete(entity)
        }
        if (flushRequired) entityManager.flush()
    }

    override fun deleteById(id: ID) {
        findById(id).ifPresent { this.delete(it) }
    }

    /** [delete] 와 대칭 — 되살린 뒤 flush 해서 증가된 version 을 호출부가 볼 수 있게 한다. */
    fun restore(entity: T) {
        if (entity is DeletableEntity && entity.useSoftDelete) {
            entity.restore()
            this.save(entity)
            entityManager.flush()
        }
    }

    fun restoreById(id: ID) {
        findById(id).ifPresent { this.restore(it) }
    }

    /** soft delete 를 적용했으면 true, 물리 삭제 대상이라 손대지 않았으면 false. flush 는 호출부가 한다. */
    private fun softDelete(entity: T): Boolean {
        if (entity is DeletableEntity && entity.useSoftDelete) {
            entity.delete()
            this.save(entity)
            return true
        }
        return false
    }

}
