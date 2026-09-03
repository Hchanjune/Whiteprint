package org.whiteprint.platform.infra.persistence.jpa.repository

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.whiteprint.platform.infra.persistence.jpa.entity.RootEntity

/**
 * [OptimizedJpaRepository] 를 실제 기반 클래스로 물려 검증한다 — 운영에서는
 * `JpaRepositoryRegistrar` 가 이 배선을 하지만 그건 다른 모듈이라 여기선 직접 건다.
 */
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = OptimizedJpaRepository::class)
class JpaVersionTestApplication

@Entity
@Table(name = "soft_deletable_items")
class SoftDeletableItem(
    @Column(name = "label")
    var label: String = "",
) : RootEntity<Long>() {

    @get:Transient
    override val useSoftDelete: Boolean get() = true
}

@Entity
@Table(name = "hard_deletable_items")
class HardDeletableItem(
    @Column(name = "label")
    var label: String = "",
) : RootEntity<Long>() {

    @get:Transient
    override val useSoftDelete: Boolean get() = false
}

/** `restore` 는 [OptimizedJpaRepository] 에만 있는 메서드라 인터페이스에 선언해야 노출된다. */
interface SoftDeletableItemRepository : JpaRepository<SoftDeletableItem, Long> {
    fun restore(entity: SoftDeletableItem)
}

interface HardDeletableItemRepository : JpaRepository<HardDeletableItem, Long>
