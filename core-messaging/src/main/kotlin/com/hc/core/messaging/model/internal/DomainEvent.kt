package com.hc.core.messaging.model.internal

interface DomainEvent: InternalEvent {
    val aggregateId: String
    val aggregateType: String
}