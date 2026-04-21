package org.whiteprint.platform.adapter.event.inbox.configuration.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.entity.EventInboxEntity

@Repository
interface JpaEventInboxRepository: JpaRepository<EventInboxEntity, Long> {

}