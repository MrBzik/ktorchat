package com.ktorchat.security.token

interface TokenService {

    fun generate(tokenConfig: TokenConfig,
                 vararg tokenClaim: TokenClaim) : String
}