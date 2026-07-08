package org.whiteprint.platform.infra.persistence.mongo.reactive.document

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Field
import org.whiteprint.platform.core.projection.model.projecion.Projection
import java.time.Instant

abstract class ProjectionDocument : Projection {

    @get:Id
    abstract override val id: String

    @get:Field("inserted_at")
    abstract override val insertedAt: Instant

    @get:Field("updated_at")
    abstract override val updatedAt: Instant

    @get:Field("is_deleted")
    abstract override val isDeleted: Boolean

    @get:Field("deleted_at")
    abstract override val deletedAt: Instant?

    @get:Transient
    override val useSoftDelete: Boolean = true

}
