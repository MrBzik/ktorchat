package com.ktorchat.data.model.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val VOICE_CALL_RESPONSE = "voice_call"

@Serializable
sealed class ServerResponse {

    abstract val type : String

    @Serializable
    @SerialName("message")
    data class MessageResponse (
        override val type: String,
        val timestamp: Long,
        val username: String,
        val userId : Long,
        val message: String,
        val receiver : Long,
        val status : Int,
    ) : ServerResponse()

    @Serializable
    @SerialName("users")
    data class UsersList(
        override val type: String,
        val users : List<UserInfo>
    ) : ServerResponse()

    @Serializable
    data class UserInfo (
        val isOnline : Boolean,
        val userName : String,
        val userId : Long,
        val profilePic : String?
    )

    @Serializable
    @SerialName("user")
    data class SingleUserInfo (
        override val type: String,
        val isOnline : Boolean,
        val userName : String,
        val userId : Long,
        val profilePic : String?
    ) : ServerResponse()


    @Serializable
    @SerialName(VOICE_CALL_RESPONSE)
    data class VoiceCallResponse(
        override val type: String,
        val receiver : Long,
        val status : Int
    ) : ServerResponse()


}