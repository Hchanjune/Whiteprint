package com.hc.service.user.command.unit.domain

import com.hc.core.domain.identifier.TsidGenerator
import com.hc.user.command.domain.model.user.User
import com.hc.user.command.domain.model.user.UserAggregate
import com.hc.user.command.domain.model.user.UserCredential
import com.hc.user.command.domain.model.user.UserProfile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class UserAggregateTest {

    @Test
    @DisplayName("UserAggregate Creation")
    fun create_user_aggregate() {
        //Given
        val email = "example@example.com"
        val password = "samplePassword"
        val passwordCheck = "samplePassword"
        val username = "sample"
        val locale = "ko-KR"
        val timeZone = "Asia/Seoul"
        val gender = "MALE"
        val phone = "010-0000-0000"
        val birthDate = "2000-01-01"
        // When
        val user = User(
            id = TsidGenerator.generate(),
            email = email,
            lastLogin = null,
            isAccountLocked = false,
            isAccountAvailable = false,
            insertedAt = Instant.now(),
            updatedAt = Instant.now(),
            isDeleted = false,
            deletedAt = null,
            version = 0
        )
        val credential = UserCredential(
            id = TsidGenerator.generate(),
            passwordHash = password,
        )
        val profile = UserProfile(
            id = TsidGenerator.generate(),
            username = username,
            locale = locale,
            timeZone = timeZone,
            gender = gender,
            phone = phone,
            birthDate = LocalDate.parse(birthDate),
        )
        // Then
        val aggregate = UserAggregate.restore(
            user = user,
            credential = credential,
            profile = profile
        )

    }

}