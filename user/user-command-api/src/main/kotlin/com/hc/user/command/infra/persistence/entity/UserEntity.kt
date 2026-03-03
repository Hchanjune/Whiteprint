package com.hc.user.command.infra.persistence.entity

import com.hc.core.jpa.entity.RootEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class UserEntity: RootEntity() {

    @Column(name = "email", length = 320, nullable = false)
    var email: String = ""
        protected set

    @Column(name = "last_login", nullable = true)
    var lastLogin: Instant? = null
        protected set

    @Column(name = "is_account_locked", nullable = false)
    var isAccountLocked: Boolean = false
        protected set

    @Column(name = "is_account_available", nullable = false)
    var isAccountAvailable: Boolean = false
        protected set

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val profile: UserProfileEntity = UserProfileEntity.create(this.id, "")
}