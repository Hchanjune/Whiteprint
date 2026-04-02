package org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.appliers

import com.zaxxer.hikari.HikariDataSource
import org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.options.OracleOption

class OracleOptionApplier(
    private val options: OracleOption
): DataSourceOptionApplier {

    init {
        throw UnsupportedOperationException("H2OptionApplier not supported yet")
    }

    override fun applyOptions(source: HikariDataSource) {
        TODO("Not yet implemented")
    }
}