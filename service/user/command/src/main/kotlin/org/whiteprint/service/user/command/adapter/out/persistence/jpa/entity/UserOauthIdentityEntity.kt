package org.whiteprint.service.user.command.adapter.out.persistence.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.whiteprint.platform.infra.persistence.jpa.entity.BranchEntity
import java.time.Instant

@Entity
@Table(name = "user_oauth_identities")
class UserOauthIdentityEntity (

    @Column(name = "provider", length = 30, nullable = false)
    var provider: String,

    @Column(name = "provider_subject", length = 200, nullable = false)
    var providerSubject: String,

    @Column(name = "email", length = 320, nullable = true)
    var email: String?,

    @Column(name = "is_email_verified", nullable = false)
    var isEmailVerified: Boolean,

    @Column(name = "linked_at", nullable = false)
    var linkedAt: Instant,

): BranchEntity<Long, UserEntity>() {

    override val useSoftDelete: Boolean = false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    override lateinit var root: UserEntity

}