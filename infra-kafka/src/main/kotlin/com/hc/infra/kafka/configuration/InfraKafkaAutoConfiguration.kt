package com.hc.infra.kafka.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(KafkaConfiguration::class)
class InfraKafkaAutoConfiguration