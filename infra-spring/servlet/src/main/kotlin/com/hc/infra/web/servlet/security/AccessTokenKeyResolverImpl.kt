package com.hc.infra.web.servlet.security

import com.hc.infra.security.verifier.model.AccessTokenVerificationKey
import com.hc.infra.security.verifier.policy.AccessTokenVerificationKeyResolver

class AccessTokenKeyResolverImpl: AccessTokenVerificationKeyResolver {
    override fun resolve(keyId: String): AccessTokenVerificationKey {
        TODO("Not yet implemented")
    }
}