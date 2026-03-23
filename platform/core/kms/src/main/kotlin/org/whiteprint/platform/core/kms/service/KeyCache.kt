package org.whiteprint.platform.core.kms.service

import org.whiteprint.platform.core.kms.model.KeyBundle
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeySide

interface KeyCache {
    fun getBundle(keyId: KeyId, side: KeySide): KeyBundle?
    fun putBundle(side: KeySide, bundle: KeyBundle)

    fun evict(keyId: KeyId)
    fun clear()
}