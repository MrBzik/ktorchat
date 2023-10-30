package com.ktorchat.data

import com.ktorchat.Constants.MESSAGE_READ
import com.ktorchat.data.model.responses.ServerResponse
import com.ktorchat.data.model.entities.Message
import com.ktorchat.data.model.mappers.toMessageResponse
import com.mongodb.client.model.Updates
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineDatabase

const val PAGE_SIZE = 8

class MessageDataSourceImpl(
    db : CoroutineDatabase
) : MessageDataSource{

    private val messages = db.getCollection<Message>()



//    override suspend fun insertPersonalMessage(
//        sender: Long,
//        receiver : Long,
//        message: Message) {
//
//        val id = "${min(sender, receiver)}#${max(sender, receiver)}"
//
//        val chat = personalMessages.findOne(PersonalChat::id eq id)
//
//        chat?.let {
//
//            val newList = chat.messages.toMutableList() + message
//
//            personalMessages.updateOne(
//                PersonalChat::id eq id,
//                Updates.set(PersonalChat::messages.name, newList)
//            )
//
//
//        } ?: run {
//            personalMessages.insertOne(
//                PersonalChat(id, listOf(message))
//            )
//        }
//    }
//
//
//    override suspend fun getPersonalChatMessages(
//        receiver: Long,  sender: Long
//    ): List<Message>? {
//
//        val id = "${min(sender, receiver)}#${max(sender, receiver)}"
//
//        return personalMessages.findOne(PersonalChat::id eq id)?.messages
//
//    }

//    override suspend fun getAllMessages(): List<Message> {
//        return messages.find()
//            .descendingSort(Message::timestamp)
//            .toList()
//    }

    override suspend fun insertMessage(message: Message) {
        messages.insertOne(message)
    }

    override suspend fun updateMessageStatus(timeStamp : Long) : Message? {
        messages.updateOne(
            Message::timestamp eq timeStamp,
            Updates.set(Message::status.name, MESSAGE_READ)
        )

        return messages.findOne(
            Message::timestamp eq timeStamp
        )
    }

    override suspend fun refreshMessages(receiver: Long, sender: Long)
    : List<ServerResponse.MessageResponse> {
        val match = messages
            .find(
                Message::receiver eq receiver,
                Message::userId eq sender)
            .sort(descending(Message::timestamp))
            .limit(1)
            .toList()


        val chatRoom = Message::receiver eq receiver
        val byTime = if(match.isEmpty()) Message::timestamp gt 0
            else Message::receiver lt match[0].timestamp
        val sendBy = if(receiver == -1L) null
            else Message::userId eq sender

        val sortBy = if (match.isEmpty()) ascending(Message::timestamp)
            else descending(Message::timestamp)

        return messages.find(
            chatRoom, byTime, sendBy
        )
            .sort(sortBy)
            .limit(PAGE_SIZE)
            .toList().map {
                it.toMessageResponse()
            }

    }

    override suspend fun getNewMessagesPage(
        receiver: Long,
        sender: Long,
        fromTime: Long): List<ServerResponse.MessageResponse> {

        val sendBy = if(receiver == -1L) null
        else Message::userId eq sender

        return messages
            .find(
                Message::receiver eq receiver,
                sendBy,
                Message::timestamp gt fromTime)
            .sort(ascending(
                Message::timestamp
            ))
            .limit(PAGE_SIZE)
            .toList().map {
                it.toMessageResponse()
            }
    }

    override suspend fun getOldMessagesPage(
        receiver: Long,
        sender: Long,
        untilTime: Long): List<ServerResponse.MessageResponse> {

        val sendBy = if(receiver == -1L) null
        else Message::userId eq sender

        return messages
            .find(
                Message::receiver eq receiver,
                sendBy,
                Message::timestamp lt untilTime)
            .sort(descending(
                Message::timestamp
            ))
            .limit(PAGE_SIZE)
            .toList().map {
                it.toMessageResponse()
            }
    }
}