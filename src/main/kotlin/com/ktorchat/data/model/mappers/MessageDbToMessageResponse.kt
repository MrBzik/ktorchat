package com.ktorchat.data.model.mappers

import com.ktorchat.data.model.entities.Message
import com.ktorchat.data.model.responses.ServerResponse

fun Message.toMessageResponse() : ServerResponse.MessageResponse {

    return ServerResponse.MessageResponse(
        type = "message",
        timestamp = timestamp,
        username = username,
        userId = userId,
        message = message,
        receiver = receiver,
        status = status
    )

}

//fun ServerResponse.MessageResponse.toMessageResponse() : ServerResponse.MessageResponse {
//
//    return ServerResponse.MessageResponse(
//        type = "message",
//        timestamp = timestamp,
//        username = username,
//        userId = userId,
//        message = message,
//        receiver = receiver,
//        status = status
//    )
//
//}