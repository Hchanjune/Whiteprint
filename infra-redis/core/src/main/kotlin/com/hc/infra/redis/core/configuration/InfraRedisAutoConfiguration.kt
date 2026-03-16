package com.hc.infra.redis.core.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(RedisConfiguration::class)
class InfraRedisAutoConfiguration