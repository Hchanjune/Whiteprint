package org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.appliers

import com.zaxxer.hikari.HikariDataSource
import org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.options.SqlServerOption

class SqlServerOptionApplier(
    private val options: SqlServerOption
): DataSourceOptionApplier {

    init {
        throw UnsupportedOperationException("SqlServerOptionApplier not supported yet")
    }

    override fun applyOptions(source: HikariDataSource) {
        TODO("Not yet implemented")
    }
}