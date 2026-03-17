package com.hc.service.user.command.adapter.out.persistence.repository

import com.hc.user.command.adapter.out.persistence.mapper.createEntity
import com.hc.user.command.adapter.out.persistence.mapper.toAggregate
import com.hc.user.command.adapter.out.persistence.mapper.updateEntity
import com.hc.user.command.application.port.out.UserRepository
import com.hc.user.command.domain.model.user.UserAggregate
import com.hc.user.command.domain.policy.user.UserPolicyException
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository
): UserRepository {
    override fun findByIdOrThrow(id: Long): UserAggregate {
        return jpaRepository.findById(id).getOrNull()?.toAggregate()
            ?: throw UserPolicyException.UserNotFoundException(id.toString())
    }

    override fun findByIdOrNull(id: Long): UserAggregate? {
        return jpaRepository.findById(id).getOrNull()?.toAggregate()
    }

    override fun create(aggregate: UserAggregate): UserAggregate {
        return aggregate.also {
            it.onCreate()
            jpaRepository.save(it.createEntity())
        }
    }

    override fun update(aggregate: UserAggregate): UserAggregate {
        return aggregate.also {
            it.onUpdate()
            //val cache = jpaRepository.getReferenceById(aggregate.idL)
            val cache = jpaRepository.findById(aggregate.idL).getOrNull()?: throw UserPolicyException.UserNotFoundException(it.id.toString())
            jpaRepository.save(it.updateEntity(cache))
        }
    }

    override fun delete(aggregate: UserAggregate) {
        aggregate.onDelete()
        jpaRepository.deleteById(aggregate.idL)
    }

    override fun updateAll(aggregates: Collection<UserAggregate>): List<UserAggregate> {
        aggregates.forEach { this.update(it) }
        return aggregates.toList()
    }

    override fun deleteAll(aggregates: Collection<UserAggregate>) {
        aggregates.forEach { this.delete(it) }
    }

    override fun findByEmailOrThrow(email: String): UserAggregate {
        return jpaRepository.findByEmail(email)?.toAggregate()
            ?: throw UserPolicyException.UserNotFoundException(email)
    }

    override fun existsByUsername(username: String): Boolean {
        return jpaRepository.existsByProfile_Username(username)
    }

    override fun existsByEmail(email: String): Boolean {
        return jpaRepository.existsByEmail(email)
    }

}