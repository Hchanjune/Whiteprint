package com.hc.core.jwt.provider

import com.hc.core.jwt.model.RefreshToken
import com.hc.core.jwt.model.RefreshTokenSubject

interface RefreshTokenProvider {

    fun generateToken(refreshTokenSubject: RefreshTokenSubject): RefreshToken

}