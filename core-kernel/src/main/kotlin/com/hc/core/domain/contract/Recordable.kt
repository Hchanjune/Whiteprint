package com.hc.core.domain.contract

interface Recordable<E> {
    val events: List<E>
}