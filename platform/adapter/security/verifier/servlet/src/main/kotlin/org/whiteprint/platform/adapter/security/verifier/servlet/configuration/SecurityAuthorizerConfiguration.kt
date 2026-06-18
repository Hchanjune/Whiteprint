package org.whiteprint.platform.adapter.security.verifier.servlet.configuration

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.whiteprint.platform.adapter.security.verifier.servlet.aspect.SecurityAuthorizationAspect
import org.whiteprint.platform.adapter.security.verifier.servlet.security.AuthorizerImpl
import org.whiteprint.platform.core.security.authorization.Authorizer

@Configuration
class SecurityAuthorizerConfiguration {

    @Bean
    @ConditionalOnMissingBean(Authorizer::class)
    fun authorizer(): Authorizer = AuthorizerImpl()

    @Bean
    fun authorizationAspect(authorizer: Authorizer): SecurityAuthorizationAspect =
        SecurityAuthorizationAspect(authorizer)

}