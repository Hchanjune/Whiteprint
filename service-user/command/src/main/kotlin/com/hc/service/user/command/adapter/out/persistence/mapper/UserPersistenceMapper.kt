package com.hc.service.user.command.adapter.out.persistence.mapper

import com.hc.infra.jpa.entity.ensureExists
import com.hc.infra.jpa.entity.withId
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
        email = this.email,
        lastLogin = this.lastLogin,
        isAccountLocked = this.isAccountLocked,
        isAccountAvailable = this.isAccountAvailable,
    ).withId(this.idL)
    userEntity.applyProfile(
        UserProfileEntity(
            username = this.username,
            locale = this.locale,
            timeZone = this.timeZone,
            gender = this.gender,
            phone = this.phone,
            birthDate = this.birthDate,
        ).withId(this.profileIdL)
    )
    userEntity.applyCredential(
        UserCredentialEntity(
            passwordHash = this.passwordHash,
        ).withId(this.credentialIdL)
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

fun UserAggregate.updateEntity(userEntity: UserEntity): UserEntity {
    userEntity.email = this.email
    userEntity.lastLogin = this.lastLogin
    userEntity.isAccountLocked = this.isAccountLocked
    userEntity.isAccountAvailable = this.isAccountAvailable
    val credentialEntity = userEntity.credential.ensureExists(userEntity.id)
    credentialEntity.passwordHash = this.passwordHash
    val profileEntity = userEntity.profile.ensureExists(userEntity.id)
    profileEntity.username = this.username
    profileEntity.locale = this.locale
    profileEntity.timeZone = this.timeZone
    profileEntity.gender = this.gender
    profileEntity.phone = this.phone
    profileEntity.birthDate = this.birthDate
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
    return userEntity
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

fun UserOauthIdentityEntity.toDomain() = UserOauthIdentity(
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

fun UserEntity.toAggregate(): UserAggregate {
    return UserAggregate.restore(
        user = this.toDomain(),
        credential = this.credential.ensureExists(this.id).toDomain(),
        profile = this.profile.ensureExists(this.id).toDomain(),
        oauthIdentities = this.oauthIdentities.map { it.toDomain() }
    )
}