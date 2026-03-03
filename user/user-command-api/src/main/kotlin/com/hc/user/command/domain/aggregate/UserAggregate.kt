package com.hc.user.command.domain.aggregate

import com.hc.user.command.domain.model.User
import com.hc.user.command.domain.model.UserCredential
import com.hc.user.command.domain.model.UserProfile

class UserAggregate(
    private val user: User,
    private val credential: UserCredential,
    private val profile: UserProfile,
) {

    val id get() = user.id.toString()
    val idL get() = user.id
    val email get() = user.email
    val lastLogin get() = user.lastLogin
    val isAccountLocked get() = user.isAccountLocked
    val isAccountAvailable get() = user.isAccountAvailable

    val credentialId get() = credential.id.toString()
    val credentialIdL get() = credential.id
    val passwordHash get() = credential.passwordHash

    val profileId get() = profile.id.toString()
    val profileIdL get() = profile.id
    val username get() = profile.username
    val locale get() = profile.locale
    val timeZone get() = profile.timeZone
    val gender get() = profile.gender
    val phone get() = profile.phone
    val birthDate get() = profile.birthDate

    val insertedAt get() = user.insertedAt
    val updatedAt get() = user.updatedAt
    val isDeleted get() = user.isDeleted
    val deletedAt get() = user.deletedAt
    val version get() = user.version


    companion object {
        fun create(
            user: User,
            credential: UserCredential,
            profile: UserProfile,
        ) = UserAggregate(
            user = user,
            credential = credential,
            profile = profile
        )
    }

}