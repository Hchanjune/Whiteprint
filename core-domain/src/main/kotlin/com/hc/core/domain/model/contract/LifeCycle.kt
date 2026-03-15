package com.hc.core.domain.model.contract

interface LifeCycle {
    fun onCreate() {}
    fun onUpdate() {}
    fun onDelete() {}
    fun onRestore() {}
}