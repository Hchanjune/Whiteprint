package org.whiteprint.platform.infra.persistence.mongo.servlet.document

import org.springframework.data.mongodb.core.mapping.Field
import java.io.Serializable
import java.time.Instant

abstract class MongoDocument<ID : Serializable> {

    abstract val id: ID
    abstract val version: Long

    @get:Field("inserted_at")
    abstract val insertedAt: Instant

    @get:Field("updated_at")
    abstract val updatedAt: Instant

    @get:Field("is_deleted")
    abstract val isDeleted: Boolean

    @get:Field("deleted_at")
    abstract val deletedAt: Instant?

    open val useSoftDelete: Boolean = false

}
