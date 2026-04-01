package org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.appliers

import com.zaxxer.hikari.HikariDataSource
import org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.options.MariadbOption

class MariadbOptionApplier(
    private val options: MariadbOption
): DataSourceOptionApplier {

    init {
        throw UnsupportedOperationException("H2OptionApplier not supported yet")
    }

    override fun applyOptions(source: HikariDataSource) {
        TODO("Not yet implemented")
    }
}
