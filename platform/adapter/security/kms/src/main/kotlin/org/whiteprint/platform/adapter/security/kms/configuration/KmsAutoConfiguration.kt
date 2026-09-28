package org.whiteprint.platform.adapter.security.kms.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(KmsConfiguration::class)
@EnableConfigurationProperties(KmsConfigurationProperties::class)
class KmsAutoConfiguration
