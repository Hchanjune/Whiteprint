package org.whiteprint.platform.core.security.provider

interface AccessTokenSigner {

    fun getKeyId(): String

    fun getAlgorithm(): String

    fun sign(data: ByteArray): ByteArray

}