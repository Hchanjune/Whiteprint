package com.hc.core.jwt.provider

import com.hc.core.jwt.model.AccessToken
import com.hc.core.jwt.model.AccessTokenSubject

interface AccessTokenProvider {

    fun generateToken(accessTokenSubject: AccessTokenSubject): AccessToken

}