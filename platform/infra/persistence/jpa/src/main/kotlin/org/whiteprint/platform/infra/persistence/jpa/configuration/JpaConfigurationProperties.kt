package org.whiteprint.platform.infra.persistence.jpa.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "platform.infra.persistence.jpa")
data class JpaConfigurationProperties (
    var datasource: DataSourceProperties = DataSourceProperties(),
    var psqlOption: PsqlOptions = PsqlOptions(),
    var mySqlOption: MySqlOptions = MySqlOptions(),
    var hikari: HikariProperties = HikariProperties(),
    var hibernate: HibernateProperties = HibernateProperties(),
    var options: Options = Options(),
) {

    data class DataSourceProperties (
        var driverClassName: String? = null,
        var jdbcUrl: String? = null,
        var username: String? = null,
        var password: String? = null,
    )

    data class PsqlOptions(
        var rewriteBatchEnabled: Boolean = false,
        var reWriteBatchedInserts: Boolean = true,
        var assumeMinServerVersion: String = "9.4",
        var tcpKeepAlive: Boolean = true
    )

    data class MySqlOptions(
        var rewriteBatchEnabled: Boolean = false,
        var rewriteBatchedStatements: Boolean = true,
        var cachePreparedStatements: Boolean = true,
        var prepStmtCacheSize: Int = 250,
        var prepStmtCacheSqlLimit: Int = 2048,
        var useServerPrepStmts: Boolean = true
    )

    data class HikariProperties(
        var maximumPoolSize: Int = 10,
        var minimumIdle: Int = 10,
        var connectionTimeoutMillis: Long = 30000,
        var idleTimeoutMillis: Long = 600000,
        var maxLifetimeMillis: Long = 1800000,
        var autoCommit: Boolean = false,
        var poolName: String = "PlatformHikariPool",
    )

    data class HibernateProperties (
        var dialect: String = "org.hibernate.dialect.PostgreSQLDialect",
        var fetchSize: Int = 1000,
        var batchSize: Int = 100,
        var providerDisablesAutocommit: Boolean = true,
        var generateStatistics: Boolean = false,
        var batchVersionedData: Boolean = true,
        var orderInserts: Boolean = true,
        var orderUpdates: Boolean = true,
        var formatSql: Boolean = true,
        var highlightSql: Boolean = true,
        var ddlAuto: String = "none",
        var defaultBatchFetchSize: Int = 100
    )

    @Suppress("ArrayInDataClass")
    data class Options(
        var packagesToScan: Array<String> = arrayOf(),
        var showSql: Boolean = false,
        var generateDdl: Boolean = false,
    )

}