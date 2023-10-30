package com.ktorchat.data.model.mappers

import com.ktorchat.data.model.responses.ServerResponse
import com.ktorchat.data.model.entities.UserDb

fun UserDb.toUserInfo() : ServerResponse.UserInfo {
    return ServerResponse.UserInfo(
//        type = "user",
        userName = username,
        userId = id,
        profilePic = profilePic,
        isOnline = false
    )
}

fun UserDb.toSingleUserInfo() : ServerResponse.SingleUserInfo {
    return ServerResponse.SingleUserInfo(
        type = "user",
        userName = username,
        userId = id,
        profilePic = profilePic,
        isOnline = false
    )
}