package org.whiteprint.sample.user.command.adapter.out.persistence.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.whiteprint.sample.user.command.adapter.out.persistence.jpa.entity.UserEntity

interface UserJpaRepository: JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun existsByProfile_Username(username: String): Boolean
}