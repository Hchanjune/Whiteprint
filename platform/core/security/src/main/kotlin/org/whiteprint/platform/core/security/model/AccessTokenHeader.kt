package org.whiteprint.platform.core.security.model

data class AccessTokenHeader(
    override val typ: String,
    override val kid: String,
    override val ver: String,
    override val alg: String
): TokenHeader