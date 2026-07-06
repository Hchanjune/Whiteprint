package org.whiteprint.sample.auth.adapter.out.persistence.jpa.repository

import io.github.hchanjune.omk.core.annotations.ManagedRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.whiteprint.sample.auth.adapter.out.persistence.jpa.mapper.AccountJpaMapper
import org.whiteprint.sample.auth.application.port.out.persistence.AccountRepository
import org.whiteprint.sample.auth.domain.accounts.aggregate.AccountAggregate
import org.whiteprint.sample.auth.domain.accounts.policy.AccountPolicy
import org.whiteprint.sample.auth.domain.accounts.policy.AccountPolicyException
import org.whiteprint.sample.auth.domain.accounts.vo.AccountIdentifier
import org.whiteprint.sample.auth.domain.accounts.vo.Email
import org.whiteprint.sample.auth.domain.accounts.vo.PhoneNumber
import org.whiteprint.sample.auth.domain.accounts.vo.Username

@Repository
@ManagedRepository
class AccountRepositoryImpl(
    private val jpaRepository: AccountJpaRepository,
    private val mapper: AccountJpaMapper,
): AccountRepository {

    override fun findByIdOrThrow(id: Long): AccountAggregate {
        return jpaRepository.findByIdOrNull(id)?.let { mapper.toAggregate(it) }
            ?: throw AccountPolicyException(
                policy = AccountPolicy.ACCOUNT_NOT_FOUND,
                attributes = mapOf(
                    "key" to "id",
                    "value" to id
                )
            )
    }

    override fun findByIdOrNull(id: Long): AccountAggregate? {
        return jpaRepository.findByIdOrNull(id)?.let { mapper.toAggregate(it) }
    }

    override fun create(aggregate: AccountAggregate): AccountAggregate {
        val entity = mapper.createEntity(aggregate)
        val savedEntity = jpaRepository.save(entity)
        return mapper.toAggregate(savedEntity)
    }

    override fun update(aggregate: AccountAggregate): AccountAggregate {
        val existingEntity = jpaRepository.findByIdOrNull(aggregate.id)
            ?: throw NoSuchElementException("수정할 계정이 존재하지 않습니다. ID: ${aggregate.id}")
        mapper.updateEntity(aggregate, existingEntity)
        return mapper.toAggregate(existingEntity)
    }

    override fun delete(aggregate: AccountAggregate) {
        jpaRepository.findByIdOrNull(aggregate.id)?.let {
            jpaRepository.delete(it)
        }
    }

    override fun updateAll(aggregates: Collection<AccountAggregate>): List<AccountAggregate> {
        return aggregates.map { update(it) }
    }

    override fun deleteAll(aggregates: Collection<AccountAggregate>) {
        aggregates.forEach { delete(it) }
    }

    override fun restore(aggregate: AccountAggregate): AccountAggregate =
        jpaRepository.findByIdOrNull(aggregate.id)
            ?.also { it.restore() }
            ?.let(jpaRepository::save)
            ?.let { mapper.toAggregate(it) }
            ?: throw AccountPolicyException(
                policy = AccountPolicy.ACCOUNT_NOT_FOUND,
                attributes = mapOf("key" to "id", "value" to aggregate.id)
            )

    override fun restoreAll(aggregates: Collection<AccountAggregate>): List<AccountAggregate> =
        aggregates.map(::restore)

    override fun existsByUsername(username: Username): Boolean {
        return jpaRepository.existsByUsername(username.value)
    }

    override fun existsByEmail(email: Email): Boolean {
        return jpaRepository.existsByEmail(email.value)
    }

    override fun existsByPhoneNumber(phoneNumber: PhoneNumber): Boolean {
        return jpaRepository.existsByPhoneNumber(phoneNumber.value)
    }

    override fun findByUsernameOrThrow(username: Username): AccountAggregate {
        return jpaRepository.findByUsername(username.value)
            ?.let { mapper.toAggregate(it) }
            ?: throw AccountPolicyException(
                policy = AccountPolicy.ACCOUNT_NOT_FOUND,
                attributes = mapOf(
                    "key" to "username",
                    "value" to username.value
                )
            )
    }

    override fun findByEmailOrThrow(email: Email): AccountAggregate {
        return jpaRepository.findByEmail(email.value)
            ?.let { mapper.toAggregate(it) }
            ?: throw AccountPolicyException(
                policy = AccountPolicy.ACCOUNT_NOT_FOUND,
                attributes = mapOf(
                    "key" to "email",
                    "value" to email.value
                )
            )
    }

    override fun findByPhoneNumberOrThrow(phoneNumber: PhoneNumber): AccountAggregate {
        return jpaRepository.findByPhoneNumber(phoneNumber.value)
            ?.let { mapper.toAggregate(it) }
            ?: throw AccountPolicyException(
                policy = AccountPolicy.ACCOUNT_NOT_FOUND,
                attributes = mapOf(
                    "key" to "phoneNumber",
                    "value" to phoneNumber.value
                )
            )
    }

    override fun findByIdentifierOrThrow(identifier: AccountIdentifier): AccountAggregate {
        return jpaRepository.findByUsernameOrEmailOrPhoneNumber(
                username = identifier.value,
                email = identifier.value,
                phoneNumber = identifier.value
            )?.let { mapper.toAggregate(it) }
            ?: throw AccountPolicyException(
                policy = AccountPolicy.ACCOUNT_NOT_FOUND,
                attributes = mapOf(
                    "key" to "identifier",
                    "value" to identifier.value
                )
            )
    }

}