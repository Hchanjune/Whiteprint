package com.hc.core.messaging.model.event

interface DomainEvent: Event {
    val aggregateId: String
    val aggregateType: String
}