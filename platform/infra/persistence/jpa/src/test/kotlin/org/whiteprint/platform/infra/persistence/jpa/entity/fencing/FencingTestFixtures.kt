package org.whiteprint.platform.infra.persistence.jpa.entity.fencing

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.whiteprint.platform.core.lock.annotation.FencingGuarded
import org.whiteprint.platform.infra.persistence.jpa.entity.RootEntity
import org.whiteprint.platform.infra.persistence.jpa.repository.OptimizedJpaRepository

/** 운영과 같은 저장소 기반 클래스([OptimizedJpaRepository])로 검증한다. */
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = OptimizedJpaRepository::class)
class JpaFencingTestApplication

@Entity
@Table(name = "fencing_guarded_items")
@FencingGuarded
class GuardedItem(
    @Column(name = "label")
    var label: String = "",
) : RootEntity<Long>() {

    @get:Transient
    override val useSoftDelete: Boolean get() = false
}

@Entity
@Table(name = "fencing_unguarded_items")
class UnguardedItem(
    @Column(name = "label")
    var label: String = "",
) : RootEntity<Long>() {

    @get:Transient
    override val useSoftDelete: Boolean get() = false
}

interface GuardedItemRepository : JpaRepository<GuardedItem, Long>

interface UnguardedItemRepository : JpaRepository<UnguardedItem, Long>
