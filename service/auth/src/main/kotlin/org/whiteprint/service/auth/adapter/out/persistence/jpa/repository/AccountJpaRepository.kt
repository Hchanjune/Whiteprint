package org.whiteprint.service.auth.adapter.out.persistence.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.whiteprint.service.auth.adapter.out.persistence.jpa.entity.AccountEntity

interface AccountJpaRepository: JpaRepository<AccountEntity, Long> {

}