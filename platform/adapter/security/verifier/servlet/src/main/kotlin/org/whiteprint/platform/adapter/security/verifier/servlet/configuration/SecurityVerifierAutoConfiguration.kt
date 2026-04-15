package org.whiteprint.platform.adapter.security.verifier.servlet.configuration


import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
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
    SecurityVerifierConfiguration::class,
    SecurityExceptionHandler::class,
)
@EnableConfigurationProperties(
    SecurityCacheConfiguration::class,
    SecurityVerifierConfigurationProperties::class,
    SecurityVerifierKmsConfigurationProperties::class,
)
class SecurityVerifierAutoConfiguration