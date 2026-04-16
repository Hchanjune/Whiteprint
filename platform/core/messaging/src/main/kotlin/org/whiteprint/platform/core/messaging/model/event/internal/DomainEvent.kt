package org.whiteprint.platform.core.messaging.model.event.internal

import org.whiteprint.platform.core.messaging.model.event.external.ExternalEvent

/**
 *
 */
interface DomainEvent: InternalEvent, ExternalEvent