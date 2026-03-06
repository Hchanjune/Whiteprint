package com.hc.core.event


interface DomainEvent: Event {
    val aggregateId: String
    val aggregateType: String
}