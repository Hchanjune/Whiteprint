package org.whiteprint.sample.user.command.domain.user.aggregate

import org.whiteprint.platform.core.domain.model.aggregate.Aggregate
import org.whiteprint.sample.user.command.domain.user.model.User
import org.whiteprint.sample.user.command.domain.user.model.UserProfile
import java.io.Serializable

class UserAggregate private constructor(
    private val user: User,
    private val profile: UserProfile,
): Aggregate<User>() {

    override val schemaVersion = "ALPHA"
    override val id: Serializable = user.id.toString()
    val idL get() = user.id
    override val root = user


    override val insertedAt get() = user.insertedAt
    override val updatedAt get() = user.updatedAt
    override val isDeleted get() = user.isDeleted
    override val deletedAt get() = user.deletedAt
    override val version get() = user.version

    companion object {

        fun create(){}

    }

    override fun toString(): String {
        return "UserAggregate(user=$user, profile=$profile, oauthIdentities=$)"
    }


}