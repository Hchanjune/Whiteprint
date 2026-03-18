package org.whiteprint.platform.infra.persistence.jpa.configuration

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import java.util.Properties
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(JpaConfigurationProperties::class)
class JpaConfiguration(
    private val jpaProperties: JpaConfigurationProperties
) {

    @Primary
    @Bean
    fun dataSource(): DataSource {
        val datasource = jpaProperties.datasource
        val hikari = jpaProperties.hikari
        val psqlOption = jpaProperties.psqlOption
        val mySqlOption = jpaProperties.mySqlOption
        return (DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .driverClassName(datasource.driverClassName)
            .url(datasource.jdbcUrl)
            .username(datasource.username)
            .password(datasource.password)
            .build() as HikariDataSource).apply {
                maximumPoolSize = hikari.maximumPoolSize
                minimumIdle = hikari.minimumIdle
                connectionTimeout = hikari.connectionTimeoutMillis
                idleTimeout = hikari.idleTimeoutMillis
                maxLifetime = hikari.maxLifetimeMillis
                isAutoCommit = hikari.autoCommit
                poolName = hikari.poolName

                // Psql Option
                if (psqlOption.rewriteBatchEnabled) {
                    addDataSourceProperty("reWriteBatchedInserts", psqlOption.reWriteBatchedInserts.toString())
                    addDataSourceProperty("assumeMinServerVersion", psqlOption.assumeMinServerVersion)
                    addDataSourceProperty("tcpKeepAlive", psqlOption.tcpKeepAlive.toString())
                }
                // MySQL Option
                if (mySqlOption.rewriteBatchEnabled) {
                    addDataSourceProperty("rewriteBatchedStatements", mySqlOption.rewriteBatchedStatements.toString())
                    addDataSourceProperty("cachePrepStmts", mySqlOption.cachePreparedStatements.toString())
                    addDataSourceProperty("prepStmtCacheSize", mySqlOption.prepStmtCacheSize.toString())
                    addDataSourceProperty("prepStmtCacheSqlLimit", mySqlOption.prepStmtCacheSqlLimit.toString())
                    addDataSourceProperty("useServerPrepStmts", mySqlOption.useServerPrepStmts.toString())
                }
            }
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

        val hibernateConfig = jpaProperties.hibernate
        val props = Properties()

        props["hibernate.dialect"] = hibernateConfig.dialect
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