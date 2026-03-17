package org.whiteprint.platform.core.kernel.policy

interface Policy {
    val status: Int
    val code: String
    val message: String
}