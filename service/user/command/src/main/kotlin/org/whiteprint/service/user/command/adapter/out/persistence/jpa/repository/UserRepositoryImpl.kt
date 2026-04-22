package org.whiteprint.service.user.command.adapter.out.persistence.jpa.repository

import org.springframework.stereotype.Repository
import org.whiteprint.service.user.command.adapter.out.persistence.jpa.mapper.createEntity
import org.whiteprint.service.user.command.adapter.out.persistence.jpa.mapper.toAggregate
import org.whiteprint.service.user.command.adapter.out.persistence.jpa.mapper.updateEntity
import org.whiteprint.service.user.command.application.port.out.persistence.UserRepository
import org.whiteprint.service.user.command.domain.user.aggregate.UserAggregate
import org.whiteprint.service.user.command.domain.user.policy.UserPolicy
import org.whiteprint.service.user.command.domain.user.policy.UserPolicyException
import kotlin.jvm.optionals.getOrNull

@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository
): UserRepository {
    override fun findByIdOrThrow(id: Long): UserAggregate {
        return jpaRepository.findById(id).getOrNull()?.toAggregate()
            ?: throw UserPolicyException(
                policy = UserPolicy.USER_NOT_FOUND,
                attributes = mapOf(
                    "userId" to id
                )
            )
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
            val cache = jpaRepository.findById(aggregate.idL).getOrNull()
                ?: throw UserPolicyException(
                    policy = UserPolicy.USER_NOT_FOUND,
                    attributes = mapOf(
                        "userId" to aggregate.idL
                    )
                )
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
            ?: throw UserPolicyException(
            policy = UserPolicy.USER_NOT_FOUND,
            attributes = mapOf(
                "email" to email
            )
        )
    }

    override fun existsByUsername(username: String): Boolean {
        return jpaRepository.existsByProfile_Username(username)
    }

    override fun existsByEmail(email: String): Boolean {
        return jpaRepository.existsByEmail(email)
    }

}