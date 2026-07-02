package org.whiteprint.platform.infra.persistence.mongo.reactive.repository

import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.support.MappingMongoEntityInformation
import org.springframework.data.mongodb.repository.support.SimpleReactiveMongoRepository
import org.whiteprint.platform.infra.persistence.mongo.reactive.document.ProjectionDocument
import reactor.core.publisher.Mono
import java.time.Instant

class ProjectionRepositorySupport<T : ProjectionDocument>(
    entityInformation: MappingMongoEntityInformation<T, String>,
    private val mongoOperations: ReactiveMongoOperations,
) : SimpleReactiveMongoRepository<T, String>(entityInformation, mongoOperations) {

    private val entityClass = entityInformation.javaType
    private val collectionName = entityInformation.collectionName

    fun upsert(document: T): Mono<Boolean> {
        val replaceQuery = Query(
            Criteria.where("_id").`is`(document.id)
                .and("version").lt(document.version)
        )
        return mongoOperations.findAndReplace(
            replaceQuery,
            document,
            FindAndReplaceOptions.options().returnNew(),
            entityClass,
            collectionName,
        ).hasElement()
            .flatMap { replaced ->
                if (replaced) Mono.just(true)
                else mongoOperations.insert(document, collectionName)
                    .thenReturn(true)
                    .onErrorResume(DuplicateKeyException::class.java) { Mono.just(false) }
            }
    }

    override fun delete(entity: T): Mono<Void> {
        if (entity.useSoftDelete) {
            val query = Query(Criteria.where("_id").`is`(entity.id))
            val update = Update()
                .set("is_deleted", true)
                .set("deleted_at", Instant.now())
            return mongoOperations.findAndModify(
                query,
                update,
                FindAndModifyOptions.options(),
                entityClass,
                collectionName,
            ).then()
        }
        return super.delete(entity)
    }

    override fun deleteById(id: String): Mono<Void> {
        return findById(id).flatMap { delete(it) }
    }

}
