package org.whiteprint.platform.core.kernel.clientContext

data class ClientContext(
    val ip: IP,
    val type: ClientType,
    val platform: PlatformType,
)