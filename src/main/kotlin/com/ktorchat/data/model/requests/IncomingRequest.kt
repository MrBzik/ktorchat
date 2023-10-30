package com.ktorchat.data.model.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class IncomingRequest {


    @Serializable
    @SerialName("voice_call")
    data class VoiceCallRequest(
        val receiver: Long,
        val status: Int
    ) : IncomingRequest()

    @Serializable
    @SerialName("message")
    data class IncomingMessage(
        val receiver : Long,
        val message : String,
        val timeStamp : Long,
        val status : Int
    ) : IncomingRequest()
}



