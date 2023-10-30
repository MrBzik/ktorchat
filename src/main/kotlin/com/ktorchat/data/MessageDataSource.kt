package com.ktorchat.data

import com.ktorchat.data.model.responses.ServerResponse
import com.ktorchat.data.model.entities.Message

interface MessageDataSource {

//    suspend fun getAllMessages() : List<Message>

    suspend fun insertMessage(message: Message)

    suspend fun updateMessageStatus(timeStamp : Long) : Message?

//    suspend fun insertPersonalMessage(sender: Long, receiver : Long, message : Message)
//
//    suspend fun getPersonalChatMessages(receiver: Long,  sender: Long) : List<Message>?

    suspend fun refreshMessages(receiver: Long, sender: Long) : List<ServerResponse.MessageResponse>

    suspend fun getNewMessagesPage(receiver: Long, sender: Long, fromTime : Long) : List<ServerResponse.MessageResponse>

    suspend fun getOldMessagesPage(receiver: Long, sender: Long, untilTime : Long) : List<ServerResponse.MessageResponse>

}