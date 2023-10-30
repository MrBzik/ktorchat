package com.ktorchat.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token : String,
    val userId : Long
)
