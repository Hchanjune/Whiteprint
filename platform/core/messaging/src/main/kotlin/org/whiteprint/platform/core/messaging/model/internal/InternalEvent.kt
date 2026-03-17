package org.whiteprint.platform.core.messaging.model.internal

import org.whiteprint.platform.core.messaging.model.Event

interface InternalEvent<out T>: Event<T>