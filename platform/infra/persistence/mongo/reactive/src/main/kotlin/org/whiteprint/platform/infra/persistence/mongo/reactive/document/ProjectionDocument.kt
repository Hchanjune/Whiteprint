package org.whiteprint.platform.infra.persistence.mongo.reactive.document

import org.springframework.data.mongodb.core.mapping.Field
import org.whiteprint.platform.core.projection.model.ProjectionModel
import java.io.Serializable
import java.time.Instant

abstract class ProjectionDocument<ID : Serializable> : ProjectionModel<ID>() {

    @get:Field("inserted_at")
    abstract override val insertedAt: Instant

    @get:Field("updated_at")
    abstract override val updatedAt: Instant

    @get:Field("is_deleted")
    abstract override val isDeleted: Boolean

    @get:Field("deleted_at")
    abstract override val deletedAt: Instant?

}
