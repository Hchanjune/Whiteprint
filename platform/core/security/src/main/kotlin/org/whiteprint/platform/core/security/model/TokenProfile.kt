package org.whiteprint.platform.core.security.model

interface TokenProfile {
    val subject: String
    val audience: Set<String>
}