package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.whiteprint.platform.core.kms.service.KeyCache
import org.whiteprint.platform.core.kms.service.KeyMaterialService
import org.whiteprint.platform.core.security.model.AccessTokenVerificationKey
import org.whiteprint.platform.core.security.verifier.AccessTokenVerificationKeyResolver

class AccessTokenKeyResolverImpl(
    private val keyCache: KeyCache,
    private val keyMaterialService: KeyMaterialService
): AccessTokenVerificationKeyResolver {

    override fun resolve(keyId: String): AccessTokenVerificationKey {
        TODO("Not yet implemented")
    }

}