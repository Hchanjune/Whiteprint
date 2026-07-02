package org.whiteprint.platform.adapter.persistence.reactive.configurations.reactiveMongo

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.persistence-reactive.reactive-mongo")
data class ReactiveMongoConfigurationProperties(
    var datasource: DataSourceProperties = DataSourceProperties(),
    var pool: PoolProperties = PoolProperties(),
    var options: Options = Options(),
) {

    data class DataSourceProperties(
        var host: String = "localhost",
        var port: Int = 27017,
        var database: String = "",
        var username: String? = null,
        var password: String? = null,
        var authDatabase: String = "admin",
    )

    data class PoolProperties(
        var maxSize: Int = 10,
        var minSize: Int = 0,
        var maxWaitMillis: Long = 2000,
        var maxConnectionIdleTimeMillis: Long = 60000,
        var maxConnectionLifeTimeMillis: Long = 300000,
    )

    data class Options(
        var repositoryPackagesToScan: MutableList<String> = mutableListOf(),
        var autoIndexCreation: Boolean = false,
    )

}
