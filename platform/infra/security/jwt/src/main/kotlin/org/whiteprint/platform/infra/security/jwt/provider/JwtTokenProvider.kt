package org.whiteprint.platform.infra.security.jwt.provider

import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenProfile
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.model.RefreshTokenProfile
import org.whiteprint.platform.core.security.policy.TokenPolicy
import org.whiteprint.platform.core.security.provider.AccessTokenSigner
import org.whiteprint.platform.core.security.provider.RefreshTokenSigner
import org.whiteprint.platform.core.security.provider.TokenProvider
import java.time.Instant
import java.util.Base64

class JwtTokenProvider(
    private val policy: TokenPolicy,
    private val accessTokenSigner: AccessTokenSigner,
    private val refreshTokenSigner: RefreshTokenSigner,
    private val serializer: Serializer,
): TokenProvider {

    private companion object {
        private val encoder = Base64.getUrlEncoder().withoutPadding()
    }

    override fun generateAccessToken(profile: AccessTokenProfile): AccessToken {
        val signingKey = accessTokenSigner.getLatestSigningKeyMetadata()
        val now = Instant.now()
        val expiresAt = now.plusSeconds(policy.accessTokenPolicy.expirationSeconds)

        val header = mapOf<String, Any>(
            "typ" to "JWT",
            "kid" to signingKey.keyAlias,
            "ver" to signingKey.keyVersion,
            "alg" to signingKey.algorithm,
        )

        val payload = mapOf<String, Any>(
            "jti" to TsidGenerator.generate().toString(),
            "sub" to profile.subject,
            "iss" to policy.accessTokenPolicy.issuer,
            "aud" to profile.audience,
            "iat" to now.epochSecond,
            "exp" to expiresAt.epochSecond,
            "prm" to profile.permissions
        )

        val headerBytes = serializer.serializeToBytes(header)
        val payloadBytes = serializer.serializeToBytes(payload)
        val encodedHeader = encoder.encodeToString(headerBytes)
        val encodedPayload = encoder.encodeToString(payloadBytes)
        val signingInput = "$encodedHeader.$encodedPayload"
        val signingResult = accessTokenSigner.sign(signingInput.toByteArray(Charsets.UTF_8))
        val encodedSignature = encoder.encodeToString(signingResult.signature)

        return AccessToken("$encodedHeader.$encodedPayload.$encodedSignature")
    }

    override fun generateRefreshToken(profile: RefreshTokenProfile): RefreshToken {
        val signingKey = refreshTokenSigner.getLatestSigningKeyMetadata()
        val now = Instant.now()
        val expiresAt = now.plusSeconds(policy.refreshTokenPolicy.expirationSeconds)

        val header = mapOf<String, Any>(
            "typ" to "JWT",
            "kid" to signingKey.keyAlias,
            "ver" to signingKey.keyVersion,
            "alg" to signingKey.algorithm,
        )

        val payload = mapOf<String, Any>(
            "jti" to TsidGenerator.generate().toString(),
            "sub" to profile.subject,
            "iss" to policy.refreshTokenPolicy.issuer,
            "aud" to profile.audience,
            "iat" to now.epochSecond,
            "exp" to expiresAt.epochSecond,
        )

        val headerBytes = serializer.serializeToBytes(header)
        val payloadBytes = serializer.serializeToBytes(payload)
        val encodedHeader = encoder.encodeToString(headerBytes)
        val encodedPayload = encoder.encodeToString(payloadBytes)
        val signingInput = "$encodedHeader.$encodedPayload"
        val signingResult = refreshTokenSigner.sign(signingInput.toByteArray(Charsets.UTF_8))
        val encodedSignature = encoder.encodeToString(signingResult.signature)

        return RefreshToken("$encodedHeader.$encodedPayload.$encodedSignature")
    }

}