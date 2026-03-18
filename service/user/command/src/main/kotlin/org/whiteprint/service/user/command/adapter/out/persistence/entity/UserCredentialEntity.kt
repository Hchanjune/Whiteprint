package org.whiteprint.service.user.command.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.whiteprint.platform.infra.persistence.jpa.entity.LeafEntity

@Entity
@Table(name = "user_credentials")
class UserCredentialEntity(

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String

): LeafEntity<Long, UserEntity>() {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    override lateinit var root: UserEntity

}