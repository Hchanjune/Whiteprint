package com.hc.user.command.infra.persistence.entity

import com.hc.core.jpa.entity.SubEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "user_profiles")
class UserProfileEntity: SubEntity() {
    @Column(name = "user_id", unique = true, nullable = false)
    var userId: Long = 0L
        protected set

    @Column(name = "username", length = 100, nullable = false)
    var username: String = ""
        protected set

    @Column(name = "locale", length = 20, nullable = true)
    var locale: String? = null
        protected set

    @Column(name = "time_zone", length = 50, nullable = true)
    var timeZone: String? = null
        protected set

    @Column(name = "gender", length = 20, nullable = true)
    var gender: String? = null
        protected set

    @Column(name = "phone", length = 20, nullable = true)
    var phone: String? = null
        protected set

    @Column(name = "birth_date", nullable = true)
    var birthDate: LocalDate? = null
        protected set

    companion object {
        fun create(
            userId: Long,
            username: String,
            locale: String? = null,
            timeZone: String? = null,
            gender: String? = null,
            phone: String? = null,
            birthDate: LocalDate? = null
        ): UserProfileEntity =
            UserProfileEntity().apply {
                this.userId = userId
                this.username = username
                this.locale = locale
                this.timeZone = timeZone
                this.gender = gender
                this.phone = phone
                this.birthDate = birthDate
            }
    }
}