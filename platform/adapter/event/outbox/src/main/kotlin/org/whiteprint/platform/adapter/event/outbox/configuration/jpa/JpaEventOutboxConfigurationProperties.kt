package org.whiteprint.platform.adapter.event.outbox.configuration.jpa

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "apater.event.outbox.jpa")
data class JpaEventOutboxConfigurationProperties(
    var datasource: DataSourceProperties = DataSourceProperties()
) {
    data class DataSourceProperties(
        var driverClassName: String = "",
        var url: String = "",
        var username: String = "",
        var password: String = "",
    )
}
