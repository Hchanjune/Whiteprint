package org.whiteprint.platform.adapter.persistence.configurations.jpa

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.Databases
import org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.appliers.DataSourceOptionApplier
import org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.appliers.H2OptionApplier
import org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.appliers.MariadbOptionApplier
import org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.appliers.MysqlOptionApplier
import org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.appliers.OracleOptionApplier
import org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.appliers.PostgresqlOptionApplier
import org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.appliers.SqlServerOptionApplier
import java.util.Properties
import javax.sql.DataSource

@Configuration
@ConditionalOnProperty(prefix = "adapter.persistence", name = ["infrastructureImplementation"], havingValue = "JPA")
class JpaConfiguration(
    private val jpaProperties: JpaConfigurationProperties
) {

    @Bean
    fun dataSourceOptionApplier(): DataSourceOptionApplier {
        return when (jpaProperties.datasource.database) {
            Databases.H2 -> H2OptionApplier(jpaProperties.h2)
            Databases.MARIADB -> MariadbOptionApplier(jpaProperties.mariadb)
            Databases.MYSQL -> MysqlOptionApplier(jpaProperties.mysql)
            Databases.ORACLE -> OracleOptionApplier(jpaProperties.oracle)
            Databases.POSTGRESQL -> PostgresqlOptionApplier(jpaProperties.postgresql)
            Databases.SQLSERVER -> SqlServerOptionApplier(jpaProperties.sqlServer)
        }
    }

    @Bean
    fun dataSource(
        dataSourceOptionApplier: DataSourceOptionApplier,
    ): DataSource = HikariDataSource().apply {
        driverClassName = jpaProperties.datasource.driverClassName
        jdbcUrl = jpaProperties.datasource.url
        username = jpaProperties.datasource.username
        password = jpaProperties.datasource.password

        maximumPoolSize = jpaProperties.hikari.maximumPoolSize
        minimumIdle = jpaProperties.hikari.minimumIdle
        connectionTimeout = jpaProperties.hikari.connectionTimeoutMillis
        idleTimeout = jpaProperties.hikari.idleTimeoutMillis
        maxLifetime = jpaProperties.hikari.maxLifetimeMillis
        isAutoCommit = jpaProperties.hikari.autoCommit
        poolName = jpaProperties.hikari.poolName
        dataSourceOptionApplier.applyOptions(this)
    }


    @Primary
    @Bean
    fun entityManagerFactory(
        dataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean {
        val factory = LocalContainerEntityManagerFactoryBean()
        factory.dataSource = dataSource
        val packagesToScan = arrayOf("org.whiteprint") + jpaProperties.options.packagesToScan
        factory.setPackagesToScan(*packagesToScan)

        val adapter = HibernateJpaVendorAdapter()
        adapter.setShowSql(jpaProperties.options.showSql)
        adapter.setGenerateDdl(jpaProperties.options.generateDdl)
        factory.jpaVendorAdapter = adapter

        val dialect = jpaProperties.datasource.database.dialect
        val hibernateConfig = jpaProperties.hibernate
        val props = Properties()

        props["hibernate.dialect"] = dialect
        props["hibernate.jdbc.fetch_size"] = hibernateConfig.fetchSize.toString()
        props["hibernate.jdbc.batch_size"] = hibernateConfig.batchSize.toString()
        props["hibernate.connection.provider_disables_autocommit"] = hibernateConfig.providerDisablesAutocommit.toString()
        props["hibernate.generate_statistics"] = hibernateConfig.generateStatistics.toString()
        props["hibernate.jdbc.batch_versioned_data"] = hibernateConfig.batchVersionedData.toString()
        props["hibernate.order_inserts"] = hibernateConfig.orderInserts.toString()
        props["hibernate.order_updates"] = hibernateConfig.orderUpdates.toString()
        props["hibernate.format_sql"] = hibernateConfig.formatSql.toString()
        props["hibernate.highlight_sql"] = hibernateConfig.highlightSql.toString()
        props["hibernate.hbm2ddl.auto"] = hibernateConfig.ddlAuto
        props["hibernate.default_batch_fetch_size"] = hibernateConfig.defaultBatchFetchSize.toString()

        factory.setJpaProperties(props)

        return factory
    }

    @Primary
    @Bean
    fun transactionManager(entityManagerFactory: LocalContainerEntityManagerFactoryBean): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory.`object`!!)
    }

}