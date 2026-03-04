package com.hc.core.domain.aggregate

interface AggregateLifeCycle {
    fun onCreate() {}
    fun onUpdate() {}
    fun onDelete() {}
    fun onRestore() {}
}