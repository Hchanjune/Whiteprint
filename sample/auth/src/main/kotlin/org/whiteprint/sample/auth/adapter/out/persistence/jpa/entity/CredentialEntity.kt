package org.whiteprint.sample.auth.adapter.out.persistence.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.whiteprint.platform.infra.persistence.jpa.entity.LeafEntity
import java.time.Instant

@Entity
@Table(name = "credentials")
class CredentialEntity(

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(name = "password_updated_at", nullable = false)
    var passwordUpdatedAt: Instant = Instant.now(),

    @Column(name = "password_expired_at", nullable = true)
    var passwordExpiredAt: Instant? = null,

    @Column(name = "failed_attempts", nullable = false)
    var failedAttempts: Int = 0,

    @Column(name = "is_locked", nullable = false)
    var isLocked: Boolean = false,

    @Column(name = "locked_reason", nullable = true)
    var lockedReason: String? = null,

    @Column(name = "locked_at", nullable = true)
    var lockedAt: Instant? = null,

    @Column(name = "last_login_at", nullable = true)
    var lastLoginAt: Instant? = null,

    @Column(name = "last_failed_at", nullable = true)
    var lastFailedAt: Instant? = null,

    @Column(name = "mfa_secret", nullable = true)
    var mfaSecret: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "password_history", nullable = true)
    var passwordHistory: String? = null,

): LeafEntity<Long, AccountEntity>() {

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    override lateinit var root: AccountEntity

}