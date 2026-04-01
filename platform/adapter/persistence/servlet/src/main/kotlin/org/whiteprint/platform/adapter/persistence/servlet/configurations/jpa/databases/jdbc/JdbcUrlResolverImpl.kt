package org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.databases.jdbc

import org.whiteprint.platform.adapter.persistence.configurations.jpa.JpaConfigurationProperties
import org.whiteprint.platform.adapter.persistence.configurations.jpa.databases.Databases
import java.net.URLEncoder
import kotlin.text.Charsets.UTF_8

class JdbcUrlResolverImpl(
    private val properties: JpaConfigurationProperties.DataSourceProperties
): JdbcUrlResolver {

    override fun resolve(): String {
        require(properties.host.isNotBlank()) { "datasource.host is required" }
        require(properties.port > 0) { "datasource.port must be positive" }
        require(properties.databaseName.isNotBlank()) { "datasource.databaseName is required" }
        require(properties.parameters.keys.none { it.isBlank() }) { "parameter key must not be blank" }
        val host = properties.host
        val port = properties.port
        val databaseName = properties.databaseName
        val parameters = properties.parameters
        return when (properties.database) {
            Databases.POSTGRESQL ->
                "jdbc:postgresql://$host:$port/$databaseName${buildQuery(parameters)}"
            Databases.MYSQL ->
                "jdbc:mysql://$host:$port/$databaseName${buildQuery(parameters)}"
            Databases.MARIADB ->
                "jdbc:mariadb://$host:$port/$databaseName${buildQuery(parameters)}"
            Databases.SQLSERVER ->
                "jdbc:sqlserver://$host:$port;databaseName=$databaseName${buildSemicolonQuery(parameters)}"
            Databases.ORACLE ->
                "jdbc:oracle:thin:@$host:$port:$databaseName"
            Databases.H2 ->
                "jdbc:h2:mem:$databaseName${buildSemicolonQuery(parameters)}"
        }
    }

    private fun buildQuery(parameters: Map<String, String>): String =
        if (parameters.isEmpty()) ""
        else parameters.entries.joinToString(
            prefix = "?",
            separator = "&"
        ) { "${URLEncoder.encode(it.key, UTF_8)}=${URLEncoder.encode(it.value, UTF_8)}" }

    private fun buildSemicolonQuery(parameters: Map<String, String>): String =
        if (parameters.isEmpty()) ""
        else parameters.entries.joinToString(
            prefix = ";",
            separator = ";"
        ) { "${URLEncoder.encode(it.key, UTF_8)}=${URLEncoder.encode(it.value, UTF_8)}" }
}