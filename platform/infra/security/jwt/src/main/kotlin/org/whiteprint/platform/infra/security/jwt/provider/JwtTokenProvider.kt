package org.whiteprint.platform.infra.security.jwt.provider

import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenHeader
import org.whiteprint.platform.core.security.model.AccessTokenPayload
import org.whiteprint.platform.core.security.model.AccessTokenProfile
import org.whiteprint.platform.core.security.model.AccessTokenSignature
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.model.RefreshTokenHeader
import org.whiteprint.platform.core.security.model.RefreshTokenPayload
import org.whiteprint.platform.core.security.model.RefreshTokenProfile
import org.whiteprint.platform.core.security.model.RefreshTokenSignature
import org.whiteprint.platform.core.security.policy.TokenPolicy
import org.whiteprint.platform.core.security.provider.AccessTokenSigner
import org.whiteprint.platform.core.security.provider.RefreshTokenSigner
import org.whiteprint.platform.core.security.provider.TokenProvider
import java.time.Instant
import java.util.Base64

class JwtTokenProvider(
    override val policy: TokenPolicy,
    private val accessTokenSigner: AccessTokenSigner,
    private val refreshTokenSigner: RefreshTokenSigner,
    private val serializer: Serializer,
): TokenProvider {

    private companion object {
        private val encoder = Base64.getUrlEncoder().withoutPadding()
    }

    override fun generateAccessToken(profile: AccessTokenProfile): AccessToken {
        val accessTokenSigningKey = accessTokenSigner.getLatestSigningKeyMetadata()
        val now = Instant.now()
        val expiresAt = now.plusSeconds(policy.accessTokenPolicy.expirationSeconds)

        val header = AccessTokenHeader(
            typ = "JWT",
            kid = accessTokenSigningKey.keyAlias,
            ver = accessTokenSigningKey.keyVersion,
            alg = accessTokenSigningKey.algorithm,
        )

        val payload = AccessTokenPayload(
            jti = TsidGenerator.generate().toString(),
            sub = profile.subject,
            iss = policy.accessTokenPolicy.issuer,
            aud = profile.audience,
            iat = now.epochSecond,
            exp = expiresAt.epochSecond,
            prm = profile.permissions
        )

        val headerBytes = serializer.serializeToBytes(header)
        val payloadBytes = serializer.serializeToBytes(payload)
        val base64Header = encoder.encodeToString(headerBytes)
        val base64Payload = encoder.encodeToString(payloadBytes)
        val base64SigningInput = "$base64Header.$base64Payload"
        val signingResult = accessTokenSigner.sign(base64SigningInput)
        val base64Signature = AccessTokenSignature(encoder.encodeToString(signingResult.signature))

        return AccessToken("$base64SigningInput.${base64Signature.signature}")
    }

    override fun generateRefreshToken(profile: RefreshTokenProfile): RefreshToken {
        val refreshTokenSigningKey = refreshTokenSigner.getLatestSigningKeyMetadata()
        val now = Instant.now()
        val expiresAt = now.plusSeconds(policy.refreshTokenPolicy.expirationSeconds)

        val header = RefreshTokenHeader(
            typ = "JWT",
            kid = refreshTokenSigningKey.keyAlias,
            ver = refreshTokenSigningKey.keyVersion,
            alg = refreshTokenSigningKey.algorithm,
        )

        val payload = RefreshTokenPayload(
            jti = TsidGenerator.generate().toString(),
            sub = profile.subject,
            iss = policy.refreshTokenPolicy.issuer,
            aud = profile.audience,
            iat = now.epochSecond,
            exp = expiresAt.epochSecond,
        )

        val headerBytes = serializer.serializeToBytes(header)
        val payloadBytes = serializer.serializeToBytes(payload)
        val base64Header = encoder.encodeToString(headerBytes)
        val base64Payload = encoder.encodeToString(payloadBytes)
        val base64SigningInput = "$base64Header.$base64Payload"
        val signingResult = refreshTokenSigner.sign(base64SigningInput)
        val base64Signature = RefreshTokenSignature(encoder.encodeToString(signingResult.signature))

        return RefreshToken("$base64SigningInput.${base64Signature.signature}")
    }

}