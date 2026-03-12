package com.hc.service.user.command.adapter.out.persistence.entity

import com.hc.infra.jpa.entity.RootEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class UserEntity(

    @Column(name = "email", length = 320, nullable = false)
    var email: String,

    @Column(name = "last_login", nullable = true)
    var lastLogin: Instant?,

    @Column(name = "is_account_locked", nullable = false)
    var isAccountLocked: Boolean,

    @Column(name = "is_account_available", nullable = false)
    var isAccountAvailable: Boolean,

): RootEntity<Long>() {

    override val useSoftDelete = true

    @OneToOne(mappedBy = "root", cascade = [CascadeType.ALL], orphanRemoval = true)
    var credential: UserCredentialEntity? = null
        protected set

    @OneToOne(mappedBy = "root", cascade = [CascadeType.ALL], orphanRemoval = true)
    var profile: UserProfileEntity? = null
        protected set

    @OneToMany(mappedBy = "root", cascade = [CascadeType.ALL], orphanRemoval = true)
    var oauthIdentities: MutableList<UserOauthIdentityEntity> = mutableListOf()
        protected set

    fun applyCredential(credential: UserCredentialEntity) {
        this.credential = credential
        credential.root = this
    }

    fun applyProfile(profile: UserProfileEntity) {
        this.profile = profile
        profile.root = this
    }

    fun addOAuthIdentity(identity: UserOauthIdentityEntity) {
        identity.root = this
        this.oauthIdentities.add(identity)
    }

    fun removeOAuthIdentity(identity: UserOauthIdentityEntity) {
        this.oauthIdentities.remove(identity)
    }

}