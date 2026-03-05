package com.hc.core.domain.contract

interface LifeCycle {
    fun onCreate() {}
    fun onUpdate() {}
    fun onDelete() {}
    fun onRestore() {}
}