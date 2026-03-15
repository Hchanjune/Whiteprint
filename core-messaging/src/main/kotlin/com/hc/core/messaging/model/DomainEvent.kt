package com.hc.core.messaging.model

interface DomainEvent: Event {
    val aggregateId: String
    val aggregateType: String
}