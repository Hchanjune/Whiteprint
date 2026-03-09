package com.hc.user.command.adapter.out.persistence.repository

import com.hc.user.command.adapter.out.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository: JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun existsByProfile_Username(username: String): Boolean
}