package org.whiteprint.platform.adapter.web.servlet.configurations

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(
    PlatformExceptionHandler::class
)
class AutoConfiguration