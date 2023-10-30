package com.ktorchat.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val userName : String,
    val userId : Long,
    val profilePic : String?
)