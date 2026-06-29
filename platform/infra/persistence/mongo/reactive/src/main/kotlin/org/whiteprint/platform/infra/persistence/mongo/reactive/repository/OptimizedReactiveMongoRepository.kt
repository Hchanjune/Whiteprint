package org.whiteprint.platform.infra.persistence.mongo.reactive.repository

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
import java.io.Serializable
import java.time.Instant

class OptimizedReactiveMongoRepository<T : ProjectionDocument<ID>, ID : Serializable>(
    entityInformation: MappingMongoEntityInformation<T, ID>,
    private val mongoOperations: ReactiveMongoOperations,
) : SimpleReactiveMongoRepository<T, ID>(entityInformation, mongoOperations) {

    private val entityClass = entityInformation.javaType
    private val collectionName = entityInformation.collectionName

    /**
     * version guard 포함 전체 document 교체 upsert.
     * 전달된 document의 version이 저장된 것보다 낮으면 stale로 판단하고 무시(false 반환).
     */
    fun upsert(document: T): Mono<Boolean> {
        val query = Query(
            Criteria.where("_id").`is`(document.id)
                .and("version").lt(document.version)
        )
        return mongoOperations.findAndReplace(
            query,
            document,
            FindAndReplaceOptions.options().upsert().returnNew(),
            entityClass,
            collectionName,
        ).hasElement()
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

    override fun deleteById(id: ID): Mono<Void> {
        return findById(id).flatMap { delete(it) }
    }

}
