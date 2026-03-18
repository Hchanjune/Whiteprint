package org.whiteprint.platform.adapter.lock.distributed.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(DistributedLockConfiguration::class)
class DistributedLockAutoConfiguration