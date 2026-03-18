package org.whiteprint.platform.adapter.lock.distributed.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.whiteprint.platform.adapter.lock.distributed.aspect.DistributedLockAspect

@Configuration
class DistributedLockConfiguration {

    @Bean
    fun distributedLockAspect(): DistributedLockAspect =
        DistributedLockAspect()

}