package org.whiteprint.platform.adapter.security.verifier.servlet.configuration


import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration
import org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration
//    (
//    before = [
//        DataRedisAutoConfiguration::class,
//        DataRedisReactiveAutoConfiguration::class
//    ]
//)
@Import(
    SecurityVerifierKmsConfiguration::class,
    SecurityCacheConfiguration::class,
    SecurityVerifierConfiguration::class
)
@EnableConfigurationProperties(
    SecurityCacheConfiguration::class,
    SecurityVerifierConfigurationProperties::class,
    SecurityVerifierKmsConfigurationProperties::class,
)
class SecurityVerifierAutoConfiguration