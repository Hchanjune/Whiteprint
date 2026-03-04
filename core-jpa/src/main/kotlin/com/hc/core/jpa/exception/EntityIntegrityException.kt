package com.hc.core.jpa.exception

class EntityIntegrityException(
    val targetName: String,
    val rootId: Any,
    override val message: String = "Entity Integrity Violation: [$targetName] not found for Root ID [$rootId]"
): RuntimeException(message)