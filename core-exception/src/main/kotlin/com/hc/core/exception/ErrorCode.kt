package com.hc.core.exception

interface ErrorCode {
    val status: Int
    val code: String
    val message: String
}