package org.whiteprint.service.auth.adapter.out.persistence.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.whiteprint.platform.infra.persistence.jpa.entity.RootEntity

@Entity
@Table(name = "accounts")
class AccountEntity(

    @Column(name = "username", length = 50, nullable = false, unique = true)
    var username: String,

    @Column(name = "email", length = 320, nullable = false, unique = true)
    var email: String,

    @Column(name = "phone_number", length = 20, nullable = false, unique = true)
    var phoneNumber: String,

): RootEntity<Long>() {

    @Transient
    override val useSoftDelete: Boolean = true

    @OneToOne(mappedBy = "root", cascade = [CascadeType.ALL], orphanRemoval = true)
    var credential: CredentialEntity? = null
        protected set

    fun applyCredential(credential: CredentialEntity) {
        this.credential = credential
        credential.root = this
    }

}