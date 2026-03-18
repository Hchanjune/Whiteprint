package org.whiteprint.platform.infra.persistence.jpa.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(JpaConfiguration::class)
class InfraJpaAutoConfiguration {
}