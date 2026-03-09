package com.hc.core.jwt.model

data class AccessTokenSubject(
    val userId: String,
    val authorities: Set<String>,
)