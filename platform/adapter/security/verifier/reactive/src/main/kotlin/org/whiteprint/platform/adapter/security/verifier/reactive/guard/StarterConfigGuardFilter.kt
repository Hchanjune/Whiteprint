package org.whiteprint.platform.adapter.security.verifier.reactive.guard

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration
import org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration
import org.springframework.boot.security.autoconfigure.actuate.web.reactive.ReactiveManagementWebSecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration

class StarterConfigGuardFilter : AutoConfigurationImportFilter {

    private val excluded = setOf(
        DataRedisAutoConfiguration::class.java.name,
        DataRedisReactiveAutoConfiguration::class.java.name,
        SecurityAutoConfiguration::class.java.name,
        UserDetailsServiceAutoConfiguration::class.java.name,
        ReactiveWebSecurityAutoConfiguration::class.java.name,
        ReactiveUserDetailsServiceAutoConfiguration::class.java.name,
        ReactiveManagementWebSecurityAutoConfiguration::class.java.name,
    )

    override fun match(
        autoConfigurationClasses: Array<out String?>,
        autoConfigurationMetadata: AutoConfigurationMetadata,
    ): BooleanArray {
        return BooleanArray(autoConfigurationClasses.size) { i ->
            val name = autoConfigurationClasses[i]
            name != null && !excluded.contains(name)
        }
    }
}
