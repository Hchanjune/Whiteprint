package org.whiteprint.platform.infra.security.jwt.provider

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.SecureDigestAlgorithm
import io.jsonwebtoken.security.SecureRequest
import io.jsonwebtoken.security.VerifySecureDigestRequest
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenProfile
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.model.RefreshTokenProfile
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import org.whiteprint.platform.core.security.policy.TokenPolicy
import org.whiteprint.platform.core.security.provider.AccessTokenSigner
import org.whiteprint.platform.core.security.provider.RefreshTokenKeyResolver
import org.whiteprint.platform.core.security.provider.TokenProvider
import java.io.InputStream
import java.security.Key
import java.time.Instant
import java.util.Date

class JwtTokenProvider(
    private val policy: TokenPolicy,
    private val accessTokenSigner: AccessTokenSigner,
    private val refreshTokenKeyResolver: RefreshTokenKeyResolver,
): TokenProvider {
    override fun generateAccessToken(
        profile: AccessTokenProfile,
    ): AccessToken {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(policy.accessTokenPolicy.expirationSeconds)
        val dummyKey = Jwts.SIG.HS256.key().build()
        val jwt = Jwts.builder()
            .header()
            .keyId(accessTokenSigner.getKeyId())
            .type("JWT")
            .and()
            .id(TsidGenerator.generate().toString())
            .subject(profile.subject)
            .issuer(policy.accessTokenPolicy.issuer)
            .audience().add(profile.audience)
            .and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .claim("authorities", profile.authorities)
            .signWith(
                dummyKey,
                object: SecureDigestAlgorithm<Key, Key> {
                    override fun getId(): String = accessTokenSigner.getAlgorithm()
                    override fun digest(request: SecureRequest<InputStream, Key>): ByteArray {
                        val inputStream = request.payload
                        val data = inputStream.readAllBytes()
                        return accessTokenSigner.sign(data)
                    }
                    override fun verify(request: VerifySecureDigestRequest<Key>): Boolean {
                        throw SecurityException(policy = SecurityPolicy.TOKEN_VERIFICATION_KEY_ERROR)
                    }
                }
            )
            .compact()
        return AccessToken(jwt)
    }

    override fun generateRefreshToken(
        profile: RefreshTokenProfile
    ): RefreshToken {
        val refreshTokenKey = refreshTokenKeyResolver.resolve()

        val now = Instant.now()
        val expiresAt = now.plusSeconds(policy.refreshTokenPolicy.expirationSeconds)

        val jwt = Jwts.builder()
            .header()
            .keyId(refreshTokenKey.keyId)
            .type("JWT")
            .and()
            .id(TsidGenerator.generate().toString())
            .subject(profile.subject)
            .issuer(policy.refreshTokenPolicy.issuer)
            .audience().add(profile.audience)
            .and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(refreshTokenKey.secretKey)
            .compact()
        return RefreshToken(jwt)
    }

}