package org.whiteprint.service.user.command.adapter.out.persistence.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.whiteprint.platform.infra.persistence.jpa.entity.LeafEntity
import java.time.LocalDate

@Entity
@Table(name = "user_profiles")
class UserProfileEntity(

    @Column(name = "username", length = 100, nullable = false)
    var username: String,

    @Column(name = "locale", length = 20, nullable = true)
    var locale: String?,

    @Column(name = "time_zone", length = 50, nullable = true)
    var timeZone: String?,

    @Column(name = "gender", length = 20, nullable = true)
    var gender: String?,

    @Column(name = "phone", length = 20, nullable = true)
    var phone: String?,

    @Column(name = "birth_date", nullable = true)
    var birthDate: LocalDate?

): LeafEntity<Long, UserEntity>() {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    override lateinit var root: UserEntity

}