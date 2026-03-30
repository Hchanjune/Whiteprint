package org.whiteprint.platform.core.persistence.model.contract

interface LifeCycle {
    fun onCreate() {}
    fun onUpdate() {}
    fun onDelete() {}
    fun onRestore() {}
}