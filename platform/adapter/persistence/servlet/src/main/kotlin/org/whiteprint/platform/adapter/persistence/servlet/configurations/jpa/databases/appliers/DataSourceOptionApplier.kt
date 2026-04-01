package org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.appliers

import com.zaxxer.hikari.HikariDataSource

interface DataSourceOptionApplier {
    fun applyOptions(source: HikariDataSource)
}