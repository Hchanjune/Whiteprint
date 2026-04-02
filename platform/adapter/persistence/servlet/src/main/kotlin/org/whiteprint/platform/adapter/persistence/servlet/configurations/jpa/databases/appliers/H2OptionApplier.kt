package org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.appliers

import com.zaxxer.hikari.HikariDataSource
import org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.options.H2Option

class H2OptionApplier(
    private val options: H2Option
): DataSourceOptionApplier {

    init {
        throw UnsupportedOperationException("H2OptionApplier not supported yet")
    }

    override fun applyOptions(source: HikariDataSource) {
        TODO("Not yet implemented")
    }
}