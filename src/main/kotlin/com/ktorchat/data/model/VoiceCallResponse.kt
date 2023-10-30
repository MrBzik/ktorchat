package com.ktorchat.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VoiceCallResponse(
    val receiver : Long,
    val status : Int
)
