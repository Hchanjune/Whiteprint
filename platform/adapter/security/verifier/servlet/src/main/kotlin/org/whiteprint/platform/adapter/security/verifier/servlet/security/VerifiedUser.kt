package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.whiteprint.platform.core.security.model.AccessTokenClaims

class VerifiedUser(
    val claims: AccessTokenClaims,
    private val authorities: Collection<GrantedAuthority>
): AbstractAuthenticationToken(authorities) {

    init {
        super.setAuthenticated(true)
    }

    override fun getName(): String = claims.subject
    override fun getPrincipal(): Any = claims
    override fun getDetails(): Any = claims
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    override fun getCredentials(): Any? = null
    override fun isAuthenticated(): Boolean = true
    override fun setAuthenticated(isAuthenticated: Boolean) {}

}