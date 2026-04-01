package org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.appliers

import com.zaxxer.hikari.HikariDataSource
import org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.options.PostgresqlOption

class PostgresqlOptionApplier(
    private val options: PostgresqlOption
): DataSourceOptionApplier {
    override fun applyOptions(source: HikariDataSource) {
        source.addDataSourceProperty("reWriteBatchEnabled", options.reWriteBatchEnabled.toString())
        source.addDataSourceProperty("reWriteBatchedInserts", options.reWriteBatchedInserts.toString())
        source.addDataSourceProperty("assumeMinServerVersion", options.assumeMinServerVersion)
        source.addDataSourceProperty("tcpKeepAlive", options.tcpKeepAlive.toString())
    }
}