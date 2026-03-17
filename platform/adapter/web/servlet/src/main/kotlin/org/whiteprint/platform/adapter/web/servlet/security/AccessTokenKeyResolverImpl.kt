package org.whiteprint.platform.adapter.web.servlet.security

import org.whiteprint.platform.adapter.security.verifier.model.AccessTokenVerificationKey
import org.whiteprint.platform.adapter.security.verifier.policy.AccessTokenVerificationKeyResolver

class AccessTokenKeyResolverImpl: AccessTokenVerificationKeyResolver {
    override fun resolve(keyId: String): AccessTokenVerificationKey {
        TODO("Not yet implemented")
    }
}