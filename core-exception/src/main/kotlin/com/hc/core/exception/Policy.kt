package com.hc.core.exception

interface Policy {
    val status: Int
    val code: String
    val message: String
}