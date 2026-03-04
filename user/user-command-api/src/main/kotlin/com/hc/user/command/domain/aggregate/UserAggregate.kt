package com.hc.user.command.domain.aggregate

import com.hc.user.command.domain.model.User
import com.hc.user.command.domain.model.UserCredential
import com.hc.user.command.domain.model.UserOauthIdentity
import com.hc.user.command.domain.model.UserProfile

data class UserAggregate(
    val user: User,
    val credential: UserCredential,
    val profile: UserProfile,
    val oauthIdentities: List<UserOauthIdentity>
) {

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
            profile = profile,
            oauthIdentities = emptyList()
        )
    }

}