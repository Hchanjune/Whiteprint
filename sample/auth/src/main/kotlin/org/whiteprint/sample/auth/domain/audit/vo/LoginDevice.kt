package org.whiteprint.sample.auth.domain.audit.vo

import org.whiteprint.platform.core.kernel.clientContext.PlatformType

@JvmInline
value class LoginDevice(val value: PlatformType)