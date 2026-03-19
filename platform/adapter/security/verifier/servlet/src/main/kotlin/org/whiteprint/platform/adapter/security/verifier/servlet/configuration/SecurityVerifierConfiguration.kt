package org.whiteprint.platform.adapter.security.verifier.servlet.configuration

import jakarta.servlet.DispatcherType
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.whiteprint.platform.adapter.security.verifier.servlet.filter.StatelessSecurityFilter
import org.whiteprint.platform.adapter.security.verifier.servlet.security.AccessTokenKeyResolverImpl
import org.whiteprint.platform.adapter.security.verifier.servlet.security.RevocationCheckerImpl
import org.whiteprint.platform.adapter.security.verifier.servlet.security.SecurityEntryPointProvider
import org.whiteprint.platform.adapter.security.verifier.core.policy.AccessTokenVerificationKeyResolver
import org.whiteprint.platform.adapter.security.verifier.core.policy.RevocationChecker
import org.whiteprint.platform.adapter.security.verifier.core.service.AccessTokenVerifier
import org.whiteprint.platform.adapter.security.verifier.core.service.DefaultAccessTokenVerifier
import org.whiteprint.platform.core.kernel.serializer.JsonSerializer
import tools.jackson.databind.ObjectMapper

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityVerifierConfigurationProperties::class)
class SecurityVerifierConfiguration {

    @Bean
    fun securityObjectMapper(): ObjectMapper = JsonSerializer.default

    @Bean
    fun securityEntryPointProvider(
        props: SecurityVerifierConfigurationProperties,
    ): SecurityEntryPointProvider =
        SecurityEntryPointProvider(props)

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun revocationChecker(): RevocationChecker
        = RevocationCheckerImpl()

    @Bean
    fun accessTokenKeyResolver(): AccessTokenVerificationKeyResolver
        = AccessTokenKeyResolverImpl()

    @Bean
    fun accessTokenVerifier(
        accessTokenKeyResolver: AccessTokenVerificationKeyResolver,
        revocationChecker: RevocationChecker
    ): AccessTokenVerifier
        = DefaultAccessTokenVerifier(
        keyResolver = accessTokenKeyResolver,
        revocationChecker = revocationChecker,
    )

    @Bean
    fun statelessSecurityFilter(
        objectMapper: ObjectMapper,
        accessTokenVerifier: AccessTokenVerifier
    ): StatelessSecurityFilter =
        StatelessSecurityFilter(
            objectMapper = objectMapper,
            accessTokenVerifier = accessTokenVerifier
        )

    @Bean
    fun statelessFilterChain(
        http: HttpSecurity,
        statelessSecurityFilter: StatelessSecurityFilter,
        securityEntryPointProvider: SecurityEntryPointProvider,
        corsConfigurationSource: CorsConfigurationSource
    ): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS Preflight 허용
                auth.requestMatchers(HttpMethod.HEAD, "/**").denyAll()

                auth.requestMatchers(*securityEntryPointProvider.permitAllMatchers().toTypedArray()).permitAll()
                auth.requestMatchers(*securityEntryPointProvider.denyAllMatchers().toTypedArray()).denyAll()
                auth.requestMatchers(*securityEntryPointProvider.authenticateAllMatchers().toTypedArray()).authenticated()

                auth.dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()

                auth.anyRequest().authenticated()
            }
            .addFilterBefore(statelessSecurityFilter, UsernamePasswordAuthenticationFilter::class.java)
            .cors { it.configurationSource(corsConfigurationSource) }
            .headers { header ->
                header.frameOptions { it.sameOrigin() }
            }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type", "traceparent")
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

}