package org.whiteprint.platform.infra.persistence.jpa.repository

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.whiteprint.platform.infra.persistence.jpa.entity.ProjectionEntity
import org.whiteprint.platform.infra.persistence.jpa.entity.RootEntity
import java.io.Serializable

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

/** 단일 PK 프로젝션 복제본. */
@Entity
@Table(name = "single_key_projections")
class SingleKeyProjectionEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: Long = 0,

    @Column(name = "label", nullable = false)
    var label: String = "",
) : ProjectionEntity()

/**
 * 복합 PK 프로젝션 복제본 — 여러 소유 도메인의 복제본을 한 테이블에 모으는 경우다.
 * 도메인마다 id 를 따로 발급하므로 `id` 만으로는 유일하지 않다.
 */
@Entity
@Table(name = "composite_key_projections")
@IdClass(CompositeKeyProjectionEntity.Key::class)
class CompositeKeyProjectionEntity(
    @Id
    @Column(name = "source_type", nullable = false, length = 64)
    var sourceType: String = "",

    @Id
    @Column(name = "id", nullable = false)
    var id: Long = 0,

    @Column(name = "label", nullable = false)
    var label: String = "",
) : ProjectionEntity() {
    data class Key(var sourceType: String = "", var id: Long = 0) : Serializable
}

interface SingleKeyProjectionRepository : JpaRepository<SingleKeyProjectionEntity, Long> {
    fun upsertProjection(entity: SingleKeyProjectionEntity): Boolean
}

interface CompositeKeyProjectionRepository :
    JpaRepository<CompositeKeyProjectionEntity, CompositeKeyProjectionEntity.Key> {
    fun upsertProjection(entity: CompositeKeyProjectionEntity): Boolean
}
