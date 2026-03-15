package com.hc.core.domain.model.contract

interface Recordable<E> {
    val events: List<E>
}