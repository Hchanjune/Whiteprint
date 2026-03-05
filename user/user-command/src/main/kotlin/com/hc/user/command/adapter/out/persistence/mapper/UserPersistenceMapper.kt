package com.hc.user.command.adapter.out.persistence.mapper

import com.hc.core.jpa.entity.ensureExists
import com.hc.core.jpa.entity.withId
import com.hc.user.command.adapter.out.persistence.entity.UserCredentialEntity
import com.hc.user.command.adapter.out.persistence.entity.UserEntity
import com.hc.user.command.adapter.out.persistence.entity.UserOauthIdentityEntity
import com.hc.user.command.adapter.out.persistence.entity.UserProfileEntity
import com.hc.user.command.domain.model.user.UserAggregate
import com.hc.user.command.domain.model.user.User
import com.hc.user.command.domain.model.user.UserCredential
import com.hc.user.command.domain.model.user.UserOauthIdentity
import com.hc.user.command.domain.model.user.UserProfile

fun UserAggregate.createEntity(): UserEntity {
    val userEntity = UserEntity(
        email = this.user.email,
        lastLogin = this.user.lastLogin,
        isAccountLocked = this.user.isAccountLocked,
        isAccountAvailable = this.user.isAccountAvailable,
    ).withId(this.user.id)
    userEntity.applyProfile(
        UserProfileEntity(
            username = this.profile.username,
            locale = this.profile.locale,
            timeZone = this.profile.timeZone,
            gender = this.profile.gender,
            phone = this.profile.phone,
            birthDate = this.profile.birthDate,
        ).withId(this.profile.id)
    )
    userEntity.applyCredential(
        UserCredentialEntity(
            passwordHash = this.credential.passwordHash,
        ).withId(this.credential.id)
    )
    this.oauthIdentities.forEach { oauthIdentity ->
        val identityEntity = UserOauthIdentityEntity(
            provider = oauthIdentity.provider,
            providerSubject = oauthIdentity.providerSubject,
            email = oauthIdentity.email,
            isEmailVerified = oauthIdentity.isEmailVerified,
            linkedAt = oauthIdentity.linkedAt,
        ).withId(oauthIdentity.id)
        userEntity.addOAuthIdentity(identityEntity)
    }
    return userEntity
}

fun UserAggregate.updateEntity(userEntity: UserEntity) {
    userEntity.email = this.user.email
    userEntity.lastLogin = this.user.lastLogin
    userEntity.isAccountLocked = this.user.isAccountLocked
    userEntity.isAccountAvailable = this.user.isAccountAvailable
    val credentialEntity = userEntity.credential.ensureExists(userEntity.id)
    credentialEntity.passwordHash = this.credential.passwordHash
    val profileEntity = userEntity.profile.ensureExists(userEntity.id)
    profileEntity.username = this.profile.username
    profileEntity.locale = this.profile.locale
    profileEntity.timeZone = this.profile.timeZone
    profileEntity.gender = this.profile.gender
    profileEntity.phone = this.profile.phone
    profileEntity.birthDate = this.profile.birthDate
    val existingIdentities = userEntity.oauthIdentities
    val domainIdentities = this.oauthIdentities
    existingIdentities.removeIf { existingEntity ->
        domainIdentities.none { it.id == existingEntity.id }
    }
    domainIdentities.forEach { newDomain ->
        val existingEntity = existingIdentities.find { it.id == newDomain.id }
        if (existingEntity != null) {
            existingEntity.provider = newDomain.provider
            existingEntity.providerSubject = newDomain.providerSubject
            existingEntity.email = newDomain.email
            existingEntity.isEmailVerified = newDomain.isEmailVerified
            existingEntity.linkedAt = newDomain.linkedAt
        } else {
            userEntity.addOAuthIdentity(
                UserOauthIdentityEntity(
                    provider = newDomain.provider,
                    providerSubject = newDomain.providerSubject,
                    email = newDomain.email,
                    isEmailVerified = newDomain.isEmailVerified,
                    linkedAt = newDomain.linkedAt,
                )
            )
        }
    }
}

fun UserEntity.toDomain() = User(
    id = this.id,
    email = this.email,
    lastLogin = this.lastLogin,
    isAccountLocked = this.isAccountLocked,
    isAccountAvailable = isAccountAvailable,
    insertedAt = this.insertedAt,
    updatedAt = this.updatedAt,
    isDeleted = this.isDeleted,
    deletedAt = this.deletedAt,
    version = this.version
)

fun UserCredentialEntity.toDomain() = UserCredential(
    id = this.id,
    passwordHash = this.passwordHash
)

fun UserProfileEntity.toDomain() = UserProfile(
    id = this.id,
    username = this.username,
    locale = this.locale,
    timeZone = this.timeZone,
    gender = this.gender,
    phone = this.phone,
    birthDate = this.birthDate
)

fun UserOauthIdentityEntity.toDomain(): UserOauthIdentity {
    return UserOauthIdentity(
        id = this.id,
        provider = this.provider,
        providerSubject = this.providerSubject,
        email = this.email,
        isEmailVerified = this.isEmailVerified,
        linkedAt = this.linkedAt,
        insertedAt = this.insertedAt,
        updatedAt = this.updatedAt,
        isDeleted = this.isDeleted,
        deletedAt = this.deletedAt,
        version = this.version
        )
}

fun UserEntity.toAggregate(): UserAggregate {
    return UserAggregate(
        user = this.toDomain(),
        credential = this.credential.ensureExists(this.id).toDomain(),
        profile = this.profile.ensureExists(this.id).toDomain(),
        oauthIdentities = this.oauthIdentities.map { it.toDomain() }
    )
}