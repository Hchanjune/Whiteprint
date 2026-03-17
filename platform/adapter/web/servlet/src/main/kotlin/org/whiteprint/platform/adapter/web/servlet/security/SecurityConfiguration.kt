package org.whiteprint.platform.adapter.web.servlet.security

import org.whiteprint.platform.core.kernel.serializer.JsonSerializer
import org.whiteprint.platform.adapter.security.verifier.policy.AccessTokenVerificationKeyResolver
import org.whiteprint.platform.adapter.security.verifier.policy.RevocationChecker
import org.whiteprint.platform.adapter.security.verifier.service.AccessTokenVerifier
import org.whiteprint.platform.adapter.security.verifier.service.DefaultAccessTokenVerifier
import org.whiteprint.platform.adapter.web.servlet.filter.StatelessSecurityFilter
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
import tools.jackson.databind.ObjectMapper

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(_root_ide_package_.org.whiteprint.platform.adapter.web.servlet.security.SecurityConfigurationProperties::class)
class SecurityConfiguration {

    @Bean
    fun securityObjectMapper(): ObjectMapper = JsonSerializer.default

    @Bean
    fun securityEntryPointProvider(
        props: org.whiteprint.platform.adapter.web.servlet.security.SecurityConfigurationProperties,
    ): org.whiteprint.platform.adapter.web.servlet.security.SecurityEntryPointProvider =
        _root_ide_package_.org.whiteprint.platform.adapter.web.servlet.security.SecurityEntryPointProvider(props)

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun revocationChecker(): RevocationChecker
        = _root_ide_package_.org.whiteprint.platform.adapter.web.servlet.security.RevocationCheckerImpl()

    @Bean
    fun accessTokenKeyResolver(): AccessTokenVerificationKeyResolver
        = _root_ide_package_.org.whiteprint.platform.adapter.web.servlet.security.AccessTokenKeyResolverImpl()

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
    ): org.whiteprint.platform.adapter.web.servlet.filter.StatelessSecurityFilter =
        _root_ide_package_.org.whiteprint.platform.adapter.web.servlet.filter.StatelessSecurityFilter(
            objectMapper = objectMapper,
            accessTokenVerifier = accessTokenVerifier
        )

    @Bean
    fun statelessFilterChain(
        http: HttpSecurity,
        statelessSecurityFilter: org.whiteprint.platform.adapter.web.servlet.filter.StatelessSecurityFilter,
        securityEntryPointProvider: org.whiteprint.platform.adapter.web.servlet.security.SecurityEntryPointProvider,
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