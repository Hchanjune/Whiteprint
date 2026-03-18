package org.whiteprint.platform.adapter.messaging.outbox.repository

import org.whiteprint.platform.adapter.messaging.outbox.entity.EventOutboxEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.whiteprint.platform.core.messaging.model.event.EventStatus

@Repository
interface JpaEventOutboxRepository: JpaRepository<EventOutboxEntity, Long> {

    @Query(
        value = """
            SELECT * FROM event_outbox
            WHERE status = 'PENDING'
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true
    )
    fun lockPending(@Param("limit") limit: Int): List<EventOutboxEntity>

    @Modifying
    @Query(value = """
        UPDATE EventOutboxEntity entity
        SET entity.status = :status
        WHERE
            entity.eventId = :eventId
    """)
    fun updateStatus(eventId: Long, status: EventStatus)

}