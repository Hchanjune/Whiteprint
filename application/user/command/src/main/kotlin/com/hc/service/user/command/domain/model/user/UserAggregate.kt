package com.hc.service.user.command.domain.model.user

import org.whiteprint.platform.core.domain.model.aggregate.Aggregate
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate

class UserAggregate private constructor(
    private val user: User,
    private val credential: UserCredential,
    private val profile: UserProfile,
    private val _oauthIdentities: List<UserOauthIdentity>,
): Aggregate<User>() {

    override val schemaVersion = "ALPHA"
    override val id: Serializable = user.id.toString()
    val idL get() = user.id
    override val root = user

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

    val oauthIdentities get() = _oauthIdentities.toList()

    override val insertedAt get() = user.insertedAt
    override val updatedAt get() = user.updatedAt
    override val isDeleted get() = user.isDeleted
    override val deletedAt get() = user.deletedAt
    override val version get() = user.version

    override fun onCreate() {
        //record(UserCreatedEvent())
        //record(UserProjectionEvent())
    }

    override fun onUpdate() {
        //record(UserUpdatedEvent())
        //record(UserProjectionEvent())
    }

    override fun onDelete() {
        //record(UserDeletedEvent())
        //record(UserProjectionEvent())
    }


    companion object {

        fun signup(
            email: String,
            password: String,
            username: String,
            locale: String?,
            timeZone: String?,
            gender: String?,
            phone: String?,
            birthDate: LocalDate?,
        ): UserAggregate {
            val user = User(
                id = TsidGenerator.generate(),
                email = email,
                lastLogin = null,
                isAccountLocked = true,
                isAccountAvailable = false,
                insertedAt = Instant.now(),
                updatedAt = Instant.now(),
                isDeleted = false,
                deletedAt = null,
                version = 0
            )
            val credential = UserCredential(
                id = TsidGenerator.generate(),
                passwordHash = password // Temporary Raw Password (Sample)
            )
            val profile = UserProfile(
                id = TsidGenerator.generate(),
                username = username,
                locale = locale,
                timeZone = timeZone,
                gender = gender,
                phone = phone,
                birthDate = birthDate,
            )
            return UserAggregate(
                user = user,
                credential = credential,
                profile = profile,
                _oauthIdentities = emptyList()
            )
        }

        fun signupOauth(
            email: String,
            provider: String,
            providerSubject: String,
            isEmailVerified: Boolean,
            username: String,
            locale: String?,
            timeZone: String?,
            gender: String?,
            phone: String?,
            birthDate: LocalDate?,
        ): UserAggregate {
            val user = User(
                id = TsidGenerator.generate(),
                email = email,
                lastLogin = null,
                isAccountLocked = true,
                isAccountAvailable = false,
                insertedAt = Instant.now(),
                updatedAt = Instant.now(),
                isDeleted = false,
                deletedAt = null,
                version = 0
            )
            val credential = UserCredential(
                id = TsidGenerator.generate(),
                passwordHash = "OAUTH"
            )
            val profile = UserProfile(
                id = TsidGenerator.generate(),
                username = username,
                locale = locale,
                timeZone = timeZone,
                gender = gender,
                phone = phone,
                birthDate = birthDate,
            )
            val oauthIdentity =  UserOauthIdentity (
                id = TsidGenerator.generate(),
                provider = provider,
                providerSubject = providerSubject,
                email = email,
                isEmailVerified = isEmailVerified,
                linkedAt = Instant.now(),
                insertedAt = Instant.now(),
                updatedAt = Instant.now(),
                isDeleted = false,
                deletedAt = null,
                version = 0
            )
            return UserAggregate(
                user = user,
                credential = credential,
                profile = profile,
                _oauthIdentities = mutableListOf(oauthIdentity)
            )
        }

        fun restore(
            user: User,
            credential: UserCredential,
            profile: UserProfile,
            oauthIdentities: List<UserOauthIdentity>,
        ) = UserAggregate(
            user = user,
            credential = credential,
            profile = profile,
            _oauthIdentities = oauthIdentities
        )
    }

    override fun toString(): String {
        return "UserAggregate(user=$user, profile=$profile, oauthIdentities=$_oauthIdentities)"
    }


}