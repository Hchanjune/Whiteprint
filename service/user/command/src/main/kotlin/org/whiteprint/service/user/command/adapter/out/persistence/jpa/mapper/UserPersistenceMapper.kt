package org.whiteprint.service.user.command.adapter.out.persistence.jpa.mapper

import org.whiteprint.platform.infra.persistence.jpa.entity.ensureExistsOrThrow
import org.whiteprint.platform.infra.persistence.jpa.entity.withId
import org.whiteprint.service.user.command.adapter.out.persistence.jpa.entity.UserCredentialEntity
import org.whiteprint.service.user.command.adapter.out.persistence.jpa.entity.UserEntity
import org.whiteprint.service.user.command.adapter.out.persistence.jpa.entity.UserOauthIdentityEntity
import org.whiteprint.service.user.command.adapter.out.persistence.jpa.entity.UserProfileEntity
import org.whiteprint.service.user.command.domain.model.user.User
import org.whiteprint.service.user.command.domain.model.user.UserAggregate
import org.whiteprint.service.user.command.domain.model.user.UserCredential
import org.whiteprint.service.user.command.domain.model.user.UserOauthIdentity
import org.whiteprint.service.user.command.domain.model.user.UserProfile


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
    val credentialEntity = userEntity.credential.ensureExistsOrThrow(userEntity.id)
    credentialEntity.passwordHash = this.passwordHash
    val profileEntity = userEntity.profile.ensureExistsOrThrow(userEntity.id)
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
        credential = this.credential.ensureExistsOrThrow(this.id).toDomain(),
        profile = this.profile.ensureExistsOrThrow(this.id).toDomain(),
        oauthIdentities = this.oauthIdentities.map { it.toDomain() }
    )
}