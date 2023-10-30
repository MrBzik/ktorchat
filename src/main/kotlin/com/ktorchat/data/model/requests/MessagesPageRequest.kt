package com.ktorchat.data.model.requests

import kotlinx.serialization.Serializable

@Serializable
data class MessagesPageRequest(
    val receiver : Long,
    val lastMessage : Long
)
