package org.whiteprint.sample.auth.adapter.`in`.web

import io.github.hchanjune.omk.core.annotations.ManagedController
import io.github.hchanjune.omk.core.annotations.ManagedOperation
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.whiteprint.platform.adapter.security.verifier.servlet.security.SecurityContextSupport
import org.whiteprint.platform.adapter.web.servlet.omk.ResponseEntityGenerator
import org.whiteprint.platform.adapter.web.servlet.request.ClientContextResolver
import org.whiteprint.platform.core.kernel.model.ApiResponse
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import org.whiteprint.sample.auth.adapter.`in`.web.mapper.toCommand
import org.whiteprint.sample.auth.adapter.`in`.web.mapper.toResponse
import org.whiteprint.sample.auth.adapter.`in`.web.request.LoginRequest
import org.whiteprint.sample.auth.adapter.`in`.web.request.LogoutRequest
import org.whiteprint.sample.auth.adapter.`in`.web.request.RefreshRequest
import org.whiteprint.sample.auth.adapter.`in`.web.request.SignupRequest
import org.whiteprint.sample.auth.adapter.`in`.web.response.LoginResponse
import org.whiteprint.sample.auth.adapter.`in`.web.response.LogoutResponse
import org.whiteprint.sample.auth.adapter.`in`.web.response.RefreshResponse
import org.whiteprint.sample.auth.adapter.`in`.web.response.SignupResponse
import org.whiteprint.sample.auth.application.port.`in`.login.LoginUseCase
import org.whiteprint.sample.auth.application.port.`in`.logout.LogoutUseCase
import org.whiteprint.sample.auth.application.port.`in`.refresh.RefreshCommand
import org.whiteprint.sample.auth.application.port.`in`.refresh.RefreshUseCase
import org.whiteprint.sample.auth.application.port.`in`.signup.SignupUseCase
import org.whiteprint.sample.auth.domain.accounts.policy.AccountPolicy
import org.whiteprint.sample.auth.domain.accounts.policy.AccountPolicyException
import java.time.Duration

@ManagedController
@RestController
@RequestMapping("/api/v1/auth")
class AuthRestController(
    private val signupUserCase: SignupUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val refreshUseCase: RefreshUseCase,
    private val clientContextResolver: ClientContextResolver,
) {

    @PostMapping("/login")
    fun login(
        request: HttpServletRequest,
        @RequestBody loginRequest: LoginRequest
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        val command = loginRequest.toCommand(clientContextResolver.resolve(request))
        val operation = loginUseCase.handle(command)
        if (operation.data.accessToken.value == "LoginFailure" || operation.data.refreshToken.value == "LoginFailure") {
            throw AccountPolicyException(
                policy = AccountPolicy.LOGIN_FAILURE,
                attributes = mapOf(
                    "failedAttempts" to operation.data.failedAttempts
                )
            )
        }
        val responseCookie = ResponseCookie.from(operation.data.refreshTokenCookieHeader, operation.data.refreshToken.value,)
            .maxAge(Duration.ofSeconds(operation.data.refreshTokenExpiration))
            .secure(true)
            .httpOnly(true)
            .sameSite("None")
            .path("/api/v1/auth/")
            .build()
        return ResponseEntityGenerator.generateFromOperation(
            cookie = responseCookie,
            operationResult = operation
        ) {
            operation.data.toResponse()
        }
    }

    @PostMapping("/logout")
    fun logout(
        @CookieValue("refresh") cookieRefreshToken: String?,
        @RequestBody(required = false) logoutRequest: LogoutRequest?,
    ): ResponseEntity<ApiResponse<LogoutResponse>> {
        val request = logoutRequest ?: LogoutRequest(
            logoutScope = LogoutRequest.Scope.CURRENT_DEVICE,
            refreshToken = null
        )
        val claims = SecurityContextSupport.getCurrentClaims()
        val command = request.toCommand(claims, cookieRefreshToken)
        val operation = logoutUseCase.handle(command)
        val expiredCookie = ResponseCookie.from("refresh", "")
            .maxAge(0)
            .secure(true)
            .httpOnly(true)
            .path("/")
            .sameSite("None")
            .build()
        return ResponseEntityGenerator.generateFromOperation(
            cookie = expiredCookie,
            operationResult = operation
        ) {
            operation.data.toResponse()
        }
    }

    @PostMapping("/signup")
    fun signup(
        @RequestBody signupRequest: SignupRequest
    ): ResponseEntity<ApiResponse<SignupResponse>> {
        val command = signupRequest.toCommand()
        val operation = signupUserCase.handle(command)
        return ResponseEntityGenerator.generateFromOperation(operation) {
            operation.data.toResponse()
        }
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(name = "refresh", required = false) cookieRefreshToken: String?,
        @RequestBody(required = false) refreshRequest: RefreshRequest?
    ): ResponseEntity<ApiResponse<RefreshResponse>> {
        val refreshToken = cookieRefreshToken
            ?: refreshRequest?.refreshToken
            ?: throw SecurityException(SecurityPolicy.TOKEN_NOT_FOUND)

        val command = RefreshCommand(RefreshToken(refreshToken))
        val operation = refreshUseCase.handle(command)

        val newCookie = ResponseCookie.from(operation.data.refreshTokenCookieHeader, operation.data.refreshToken.value,)
            .maxAge(operation.data.refreshTokenExpiration)
            .secure(true)
            .httpOnly(true)
            .path("/api/v1/auth/")
            .sameSite("None")
            .build()
        return ResponseEntityGenerator.generateFromOperation(
            cookie = newCookie,
            operationResult = operation
        ) {
            operation.data.toResponse()
        }
    }

    @ManagedOperation("validate")
    @PostMapping("/validate")
    fun validate(): ResponseEntity<ApiResponse<AccessTokenClaims>> {
        return ResponseEntityGenerator.generateInstantData(
            data = SecurityContextSupport.getCurrentClaims(),
            message = "Successfully validated"
        )
    }

}