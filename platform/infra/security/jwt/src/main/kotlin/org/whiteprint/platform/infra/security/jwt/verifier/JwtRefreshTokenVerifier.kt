package org.whiteprint.platform.infra.security.jwt.verifier

import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.security.model.AccessTokenHeader
import org.whiteprint.platform.core.security.model.AccessTokenPayload
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.model.RefreshTokenClaims
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import org.whiteprint.platform.core.security.verifier.RefreshTokenVerificationKeyResolver
import org.whiteprint.platform.core.security.verifier.RevocationChecker
import org.whiteprint.platform.core.security.verifier.RefreshTokenVerifier
import java.time.Instant
import java.util.Base64

class JwtRefreshTokenVerifier (
    private val serializer: Serializer,
    private val keyResolver: RefreshTokenVerificationKeyResolver,
    private val revocationChecker: RevocationChecker
): RefreshTokenVerifier {

    private companion object {
        private val decoder = Base64.getUrlDecoder()
    }

    override fun verifyOrThrow(token: RefreshToken): RefreshTokenClaims {
        val now = Instant.now()
        val parts = token.value.split(".")

        if (parts.size != 3) {
            throw SecurityException(SecurityPolicy.TOKEN_INVALID)
        }

        val base64Header = parts[0]
        val base64Payload = parts[1]
        val base64Signature = parts[2]

        val headerBytes = decoder.decode(base64Header)
        val payloadBytes = decoder.decode(base64Payload)
        val signatureBytes = decoder.decode(base64Signature)

        val header = try {
            serializer.deserializeFromBytes(headerBytes, AccessTokenHeader::class.java)
        } catch (_: Exception) {
            throw SecurityException(SecurityPolicy.TOKEN_INVALID)
        }

        val refreshTokenVerificationKey = keyResolver.resolve(header.kid, header.ver)

        try {
            val dataToVerify = "$base64Header.$base64Payload".toByteArray(Charsets.UTF_8)
            val signature = java.security.Signature.getInstance("SHA256withRSA")
            signature.initVerify(refreshTokenVerificationKey.verifyKey)
            signature.update(dataToVerify)
            signature.verify(signatureBytes)
        } catch (_: Exception) {
            throw SecurityException(SecurityPolicy.TOKEN_INVALID)
        }


        val payload = try {
            serializer.deserializeFromBytes(payloadBytes, AccessTokenPayload::class.java)
        } catch (_: Exception) {
            throw SecurityException(SecurityPolicy.TOKEN_INVALID)
        }



        if (Instant.ofEpochSecond(payload.iat).isAfter(now)) {
            throw SecurityException(SecurityPolicy.TOKEN_INVALID)
        }

        if (Instant.ofEpochSecond(payload.exp).isBefore(now)) {
            throw SecurityException(SecurityPolicy.TOKEN_EXPIRED)
        }

//        if (payload.iss != expectedIssuer) {
//            throw SecurityException(SecurityPolicy.TOKEN_INVALID)
//        }

//        if (expectedAudience !in payload.aud) {
//            throw SecurityException(SecurityPolicy.TOKEN_INVALID)
//        }

        val refreshTokenClaims = try {
            RefreshTokenClaims(
                tokenId = payload.jti,
                subject = payload.sub,
                issuer = payload.iss,
                audience = payload.aud,
                issuedAt = Instant.ofEpochSecond(payload.iat),
                expiresAt = Instant.ofEpochSecond(payload.exp),
            )
        } catch (_: Exception) {
            throw SecurityException(SecurityPolicy.TOKEN_CLAIM_INVALID)
        }

        revocationChecker.assertNotRevoked(refreshTokenClaims)

        return refreshTokenClaims
    }

}