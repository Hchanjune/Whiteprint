package org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.appliers

import com.zaxxer.hikari.HikariDataSource
import org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.options.MysqlOption

class MysqlOptionApplier(
    private val options: MysqlOption
): DataSourceOptionApplier {
    override fun applyOptions(source: HikariDataSource) {
        source.addDataSourceProperty("rewriteBatchedStatements", options.rewriteBatchedStatements.toString())
        source.addDataSourceProperty("cachePrepStmts", options.cachePreparedStatements.toString())
        source.addDataSourceProperty("prepStmtCacheSize", options.prepStmtCacheSize.toString())
        source.addDataSourceProperty("prepStmtCacheSqlLimit", options.prepStmtCacheSqlLimit.toString())
        source.addDataSourceProperty("useServerPrepStmts", options.useServerPrepStmts.toString())
    }
}