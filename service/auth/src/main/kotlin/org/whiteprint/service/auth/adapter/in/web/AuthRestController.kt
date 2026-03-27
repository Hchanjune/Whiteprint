package org.whiteprint.service.auth.adapter.`in`.web

import org.springframework.http.ResponseEntity
import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.cache.operation.get
import org.whiteprint.platform.core.kernel.identifier.TsidGenerator
import org.whiteprint.platform.core.kernel.model.ApiResponse
import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.provider.TokenProvider
import java.time.Instant

@ManagedResource
@RestController
@RequestMapping("/api/v1/auth")
class AuthRestController(
    private val valueOperations: ValueOperations,
    private val tokenProvider: TokenProvider,
) {

    @GetMapping("/test")
    fun test(): String {
        return valueOperations.get<String>(CacheKey("123")).toString()
    }

    @PostMapping("/login")
    fun login(): ResponseEntity<ApiResponse<AccessToken>> {
        val accessTokenClaims = AccessTokenClaims(
            tokenId = TsidGenerator.generate().toString(),
            subject = "UserUUID",
            issuer = "",
            audience = emptySet(),
            issuedAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(60),
            authorities = emptySet(),
        )
        val token = tokenProvider.generateAccessToken(accessTokenClaims)
        val response = ApiResponse.success(
            id = "",
            data = token,
            traceId = null,
            message = ""
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    fun logout() {

    }

    @PostMapping("/refresh")
    fun refresh() {

    }

}