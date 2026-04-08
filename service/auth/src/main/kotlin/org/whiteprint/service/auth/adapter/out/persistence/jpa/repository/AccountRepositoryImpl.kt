package org.whiteprint.service.auth.adapter.out.persistence.jpa.repository

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.whiteprint.service.auth.adapter.out.persistence.jpa.mapper.AccountJpaMapper
import org.whiteprint.service.auth.application.port.out.AccountRepository
import org.whiteprint.service.auth.domain.accounts.aggregate.AccountAggregate

@Repository
class AccountRepositoryImpl(
    private val jpaRepository: AccountJpaRepository,
    private val mapper: AccountJpaMapper,
): AccountRepository {

    override fun findByIdOrThrow(id: Long): AccountAggregate {
        return jpaRepository.findByIdOrNull(id)?.let { mapper.toAggregate(it) }
            ?: throw NoSuchElementException("")
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

}