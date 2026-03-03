package com.hc.user.command.infra.persistence.entity

import com.hc.core.jpa.entity.SubEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "user_credentials")
class UserCredentialEntity(
    userId: Long,
    passwordHash: String
): SubEntity() {
    @Column(name = "user_id", unique = true, nullable = false)
    var userId: Long = userId
        protected set
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String = passwordHash
        protected set
}