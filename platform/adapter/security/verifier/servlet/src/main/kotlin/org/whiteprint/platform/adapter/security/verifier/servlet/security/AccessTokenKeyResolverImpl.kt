package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.whiteprint.platform.adapter.security.verifier.core.model.AccessTokenVerificationKey
import org.whiteprint.platform.adapter.security.verifier.core.policy.AccessTokenVerificationKeyResolver


class AccessTokenKeyResolverImpl: AccessTokenVerificationKeyResolver {
    override fun resolve(keyId: String): AccessTokenVerificationKey {
        TODO("Not yet implemented")
    }
}