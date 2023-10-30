package com.ktorchat.security.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JwtTokenService : TokenService {

    override fun generate(tokenConfig: TokenConfig, vararg tokenClaim: TokenClaim): String {
        var token = JWT.create()
            .withAudience(tokenConfig.audience)
            .withIssuer(tokenConfig.issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + tokenConfig.expiresIn))
        tokenClaim.forEach {
            token = token.withClaim(it.name, it.value)
        }
        return token.sign(Algorithm.HMAC256(tokenConfig.secret))
    }
}