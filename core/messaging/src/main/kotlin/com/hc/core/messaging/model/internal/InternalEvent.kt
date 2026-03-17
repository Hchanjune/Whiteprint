package com.hc.core.messaging.model.internal

import com.hc.core.messaging.model.Event

interface InternalEvent<out T>: Event<T>