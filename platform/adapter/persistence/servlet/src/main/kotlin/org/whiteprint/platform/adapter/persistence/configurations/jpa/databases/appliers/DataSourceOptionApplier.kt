package org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.appliers

import com.zaxxer.hikari.HikariDataSource

interface DataSourceOptionApplier {
    fun applyOptions(source: HikariDataSource)
}