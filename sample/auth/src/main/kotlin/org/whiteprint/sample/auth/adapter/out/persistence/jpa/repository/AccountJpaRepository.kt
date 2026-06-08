package org.whiteprint.sample.auth.adapter.out.persistence.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.whiteprint.sample.auth.adapter.out.persistence.jpa.entity.AccountEntity

interface AccountJpaRepository: JpaRepository<AccountEntity, Long> {

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun existsByPhoneNumber(phoneNumber: String): Boolean

    fun findByUsername(username: String): AccountEntity?

    fun findByEmail(email: String): AccountEntity?

    fun findByPhoneNumber(phoneNumber: String): AccountEntity?

    fun findByUsernameOrEmailOrPhoneNumber(username: String, email: String, phoneNumber: String): AccountEntity?

}