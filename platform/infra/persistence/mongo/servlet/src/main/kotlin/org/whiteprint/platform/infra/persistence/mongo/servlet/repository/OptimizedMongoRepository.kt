package org.whiteprint.platform.infra.persistence.mongo.servlet.repository

import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.support.MappingMongoEntityInformation
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository
import org.whiteprint.platform.infra.persistence.mongo.servlet.document.MongoDocument
import java.io.Serializable
import java.time.Instant

class OptimizedMongoRepository<T : MongoDocument<ID>, ID : Serializable>(
    entityInformation: MappingMongoEntityInformation<T, ID>,
    private val mongoOperations: MongoOperations,
) : SimpleMongoRepository<T, ID>(entityInformation, mongoOperations) {

    private val entityClass = entityInformation.javaType
    private val collectionName = entityInformation.collectionName

    fun upsert(document: T): Boolean {
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
        ) != null
    }

    override fun delete(entity: T) {
        if (entity.useSoftDelete) {
            val query = Query(Criteria.where("_id").`is`(entity.id))
            val update = Update()
                .set("is_deleted", true)
                .set("deleted_at", Instant.now())
            mongoOperations.findAndModify(query, update, FindAndModifyOptions.options(), entityClass, collectionName)
            return
        }
        super.delete(entity)
    }

    override fun deleteById(id: ID) {
        findById(id).ifPresent { delete(it) }
    }

}
