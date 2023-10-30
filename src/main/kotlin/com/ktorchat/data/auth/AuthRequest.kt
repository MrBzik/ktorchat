package com.ktorchat.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val username : String,
    val password : String
)
