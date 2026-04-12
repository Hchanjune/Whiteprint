package org.whiteprint.platform.infra.security.jwt.verifier

import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.verifier.AccessTokenVerificationKeyResolver
import org.whiteprint.platform.core.security.verifier.RevocationChecker
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import org.whiteprint.platform.core.security.verifier.AccessTokenVerifier
import org.whiteprint.platform.core.security.policy.SecurityException
import java.time.Instant
import java.util.Base64

class JwtAccessTokenVerifier (
    private val serializer: Serializer,
    private val keyResolver: AccessTokenVerificationKeyResolver,
    private val revocationChecker: RevocationChecker
): AccessTokenVerifier {

    private companion object {
        private val decoder = Base64.getUrlDecoder()
    }

    override fun verifyOrThrow(token: AccessToken): AccessTokenClaims {
        val parts = token.value.split(".")

        require(parts.size == 3) {
            throw SecurityException(SecurityPolicy.TOKEN_INVALID)
        }

        val encodedHeader = parts[0]
        val encodedPayload = parts[1]
        val encodedSignature = parts[2]

        val headerBytes = decoder.decode(encodedHeader)
        val payloadBytes = decoder.decode(encodedPayload)
        val signatureBytes = decoder.decode(encodedSignature)

        @Suppress("UNCHECKED_CAST")
        val header = serializer.deserializeFromBytes(headerBytes, Map::class.java) as Map<String, Any>

        @Suppress("UNCHECKED_CAST")
        val payload = serializer.deserializeFromBytes(payloadBytes, Map::class.java) as Map<String, Any>

        val accessTokenClaims = try {
            AccessTokenClaims(
                tokenId = payload["jti"] as String,
                subject = payload["sub"] as String,
                issuer = payload["iss"] as String,
                audience = when (val aud = payload["aud"]) {
                    is String -> setOf(aud)
                    is Collection<*> -> aud.map { it.toString() }.toSet()
                    else -> emptySet()
                },
                issuedAt = Instant.ofEpochSecond((payload["iat"] as Number).toLong()),
                expiresAt = Instant.ofEpochSecond((payload["exp"] as Number).toLong()),
                permissions = (payload["prm"] as? Collection<*>)
                    ?.map { it.toString() }
                    ?.toSet()
                    ?: emptySet()
            )
        } catch (_: Exception) {
            throw SecurityException(SecurityPolicy.TOKEN_CLAIM_INVALID)
        }

        println(accessTokenClaims.toString())

        revocationChecker.assertNotRevoked(accessTokenClaims)

        return accessTokenClaims
    }

//    override fun verifyOrThrow(token: AccessToken): AccessTokenClaims {
//        return try {
//            val claims = Jwts.parser()
//                .keyLocator { header: Header ->
//                    val kid = header["kid"] as? String
//                        ?: throw SecurityException(SecurityPolicy.TOKEN_KEY_ID_MISSING)
//                    val ver = header["ver"] as? String
//                    println("Debug: $kid")
//                    println("Debug: $ver")
//                    keyResolver.resolve(kid, ver).verifyKey.also {
//                        println("Debug: $it")
//                    }
//                }
//                .build()
//                .parseSignedClaims(token.value)
//                .payload
//
//            val accessTokenClaims = AccessTokenClaims(
//                tokenId = claims.id,
//                subject = claims.subject,
//                issuer = claims.issuer,
//                audience = claims.audience,
//                issuedAt = claims.issuedAt.toInstant(),
//                expiresAt = claims.expiration.toInstant(),
//                authorities = (claims["prm"] as? Iterable<*>)?.map { it.toString() }?.toSet() ?: emptySet()
//            )
//
//            revocationChecker.assertNotRevoked(accessTokenClaims)
//
//            accessTokenClaims
//
//        } catch (exception: Exception) {
//            exception.printStackTrace()
//            if (exception is JwtException) {
//                throw JwtExceptionMapper.mapFrom(exception)
//            } else {
//                throw exception
//            }
//        }
//    }

}