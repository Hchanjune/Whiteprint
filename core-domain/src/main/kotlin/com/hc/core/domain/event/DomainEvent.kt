package com.hc.core.domain.event


interface DomainEvent: Event {
    val aggregateId: String
    val aggregateType: String
}