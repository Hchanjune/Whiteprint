package com.hc.infra.cache.redis.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(_root_ide_package_.com.hc.infra.cache.redis.configuration.RedisConfiguration::class)
class InfraRedisAutoConfiguration