package com.hc.infra.security.verifier.model

import java.security.PublicKey

data class AccessTokenVerificationKey(
    val keyId: String,
    val verifyKey: PublicKey
)