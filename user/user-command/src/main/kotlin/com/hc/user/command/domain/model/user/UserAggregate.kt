package com.hc.user.command.domain.model.user

import com.hc.core.domain.aggregate.Aggregate
import java.io.Serializable

data class UserAggregate(
    val user: User,
    val credential: UserCredential,
    val profile: UserProfile,
    val oauthIdentities: List<UserOauthIdentity>,
): Aggregate<User>() {

    override val id: Serializable = user.id
    override val root = user

    override val insertedAt get() = user.insertedAt
    override val updatedAt get() = user.updatedAt
    override val isDeleted get() = user.isDeleted
    override val deletedAt get() = user.deletedAt
    override val version get() = user.version

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