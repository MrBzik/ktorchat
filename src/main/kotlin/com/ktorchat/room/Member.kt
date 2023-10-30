package com.ktorchat.room

import io.ktor.websocket.*

data class Member(
    val userId : String,
    val websocket : WebSocketSession
)
