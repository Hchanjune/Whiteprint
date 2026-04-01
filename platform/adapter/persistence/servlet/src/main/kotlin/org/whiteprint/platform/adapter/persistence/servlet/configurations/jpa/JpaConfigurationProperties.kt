package org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa

import org.springframework.boot.context.properties.ConfigurationProperties
import org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.Databases
import org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.options.H2Option
import org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.options.MariadbOption
import org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.options.MysqlOption
import org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.options.OracleOption
import org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.options.PostgresqlOption
import org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.options.SqlServerOption

@ConfigurationProperties(prefix = "adapter.persistence.jpa")
data class JpaConfigurationProperties (
    var datasource: DataSourceProperties = DataSourceProperties(),
    var hikari: HikariProperties = HikariProperties(),
    var hibernate: HibernateProperties = HibernateProperties(),
    var options: Options = Options(),

    var h2: H2Option = H2Option(),
    var mariadb: MariadbOption = MariadbOption(),
    var mysql: MysqlOption = MysqlOption(),
    var oracle: OracleOption = OracleOption(),
    var postgresql: PostgresqlOption = PostgresqlOption(),
    var sqlServer: SqlServerOption = SqlServerOption()
) {

    data class DataSourceProperties (
        var database: Databases = Databases.POSTGRESQL,
        var driverClassName: String? = null,

        var host: String = "",
        var port: Int = 5432,
        var databaseName: String = "",

        var parameters: Map<String, String> = emptyMap(),

        var username: String = "",
        var password: String = "",
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

    data class Options(
        var packagesToScan: MutableList<String> = mutableListOf(),
        var showSql: Boolean = false,
        var generateDdl: Boolean = false,
    )

}