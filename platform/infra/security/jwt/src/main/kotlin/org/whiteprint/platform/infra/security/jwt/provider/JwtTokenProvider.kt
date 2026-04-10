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
import org.whiteprint.platform.core.security.provider.RefreshTokenSigner
import org.whiteprint.platform.core.security.provider.TokenProvider
import java.io.InputStream
import java.security.Key
import java.time.Instant
import java.util.Date

class JwtTokenProvider(
    private val policy: TokenPolicy,
    private val accessTokenSigner: AccessTokenSigner,
    private val refreshTokenSigner: RefreshTokenSigner,
): TokenProvider {

    private companion object {
        val dummyKey = Jwts.SIG.RS256.keyPair().build().private
    }

    override fun generateAccessToken(
        profile: AccessTokenProfile,
    ): AccessToken {
        val signingKey = accessTokenSigner.getLatestSigningKeyMetadata()
        val now = Instant.now()
        val expiresAt = now.plusSeconds(policy.accessTokenPolicy.expirationSeconds)

        val jwt = Jwts.builder()
            .header()
                .keyId(signingKey.keyAlias)
                .add("ver", signingKey.keyVersion)
                .type("JWT")
            .and()
                .id(TsidGenerator.generate().toString())
                .subject(profile.subject)
                .issuer(policy.accessTokenPolicy.issuer)
                .audience().add(profile.audience)
            .and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
            .claim("prm", profile.permissions)
            .signWith(
                dummyKey,
                object: SecureDigestAlgorithm<Key, Key> {
                    override fun getId(): String = signingKey.algorithm
                    override fun digest(request: SecureRequest<InputStream, Key>): ByteArray {
                        val contentToSign = request.payload.readAllBytes()
                        println("DEBUG: Signing Input String -> ${String(contentToSign)}")
                        val result = accessTokenSigner.sign(contentToSign)
                        println("DEBUG: Signature Length -> ${result.signature.size}")
                        return result.signature
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
        val signingKey = refreshTokenSigner.getLatestSigningKeyMetadata()
        val now = Instant.now()
        val expiresAt = now.plusSeconds(policy.refreshTokenPolicy.expirationSeconds)

        val claims = Jwts.claims()
            .id(TsidGenerator.generate().toString())
            .subject(profile.subject)
            .issuer(policy.refreshTokenPolicy.issuer)
            .audience().add(profile.audience)
            .and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .build()

        val jwt = Jwts.builder()
            .header()
                .keyId(signingKey.keyAlias)
                .add("ver", signingKey.keyVersion)
                .type("JWT")
            .and()
                .id(TsidGenerator.generate().toString())
                .subject(profile.subject)
                .issuer(policy.refreshTokenPolicy.issuer)
                .audience().add(profile.audience)
            .and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
            .signWith(
                dummyKey,
                object: SecureDigestAlgorithm<Key, Key> {
                    override fun getId(): String = signingKey.algorithm
                    override fun digest(request: SecureRequest<InputStream, Key>): ByteArray {
                        val contentToSign = request.payload.readAllBytes()
                        val result = refreshTokenSigner.sign(contentToSign)
                        return result.signature
                    }
                    override fun verify(request: VerifySecureDigestRequest<Key>): Boolean {
                        throw SecurityException(policy = SecurityPolicy.TOKEN_VERIFICATION_KEY_ERROR)
                    }
                }
            )
            .compact()
        return RefreshToken(jwt)
    }

}