package org.whiteprint.platform.core.security.authorization

interface AuthorizedPermission {
    val value: String
    val priority: Int
}