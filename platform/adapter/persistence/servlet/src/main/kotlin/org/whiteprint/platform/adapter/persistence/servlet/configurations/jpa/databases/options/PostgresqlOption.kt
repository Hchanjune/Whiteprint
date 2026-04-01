package org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.options

data class PostgresqlOption(
    var reWriteBatchEnabled: Boolean = false,
    var reWriteBatchedInserts: Boolean = true,
    var assumeMinServerVersion: String = "9.4",
    var tcpKeepAlive: Boolean = true
)