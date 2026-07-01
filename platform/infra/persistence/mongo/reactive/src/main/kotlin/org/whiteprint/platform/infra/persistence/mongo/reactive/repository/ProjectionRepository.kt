package org.whiteprint.platform.infra.persistence.mongo.reactive.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.whiteprint.platform.infra.persistence.mongo.reactive.document.ProjectionDocument

interface ProjectionRepository<T : ProjectionDocument> : CoroutineCrudRepository<T, String> {
    suspend fun upsert(document: T): Boolean
}
