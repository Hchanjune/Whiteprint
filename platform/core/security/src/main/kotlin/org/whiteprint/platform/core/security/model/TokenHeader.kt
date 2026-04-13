package org.whiteprint.platform.core.security.model

interface TokenHeader {
    val typ: String
    val kid: String
    val ver: String
    val alg: String
}