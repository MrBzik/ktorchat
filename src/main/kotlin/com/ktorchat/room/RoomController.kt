package com.ktorchat.room

import com.ktorchat.Constants.MESSAGE_SENT
import com.ktorchat.Constants.VOICE_CALL_ACCEPT
import com.ktorchat.Constants.VOICE_CALL_DECLINE
import com.ktorchat.Constants.VOICE_CALL_REQUEST
import com.ktorchat.data.MessageDataSource
import com.ktorchat.data.UserDataSource
import com.ktorchat.data.model.requests.IncomingRequest
import com.ktorchat.data.model.entities.Message
import com.ktorchat.data.model.responses.ServerResponse
import com.ktorchat.data.model.entities.UserDb
import com.ktorchat.data.model.mappers.toMessageResponse
import com.ktorchat.data.model.responses.VOICE_CALL_RESPONSE

import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class RoomController(
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource
) {


    private val json = Json {
        classDiscriminator = "type"
    }

    data class UserSession(
        val socket: WebSocketSession,
        val inVoiceCall : Long? = null
    )


    private val users = ConcurrentHashMap<Long, UserSession>()

    suspend fun join(
        userId : Long,
        socket : WebSocketSession
    ){

        if(users.containsKey(userId)){
            println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!ALREADY EXISTS!!!!!!!!!!!!!!!!!!!!")
            throw MemberAlreadyExistsException()
        }


        println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ID : $userId")

        users[userId] = UserSession(socket)


        sendUsersList()

    }


    suspend fun updateMessageStatus(
        timeStamp: Long
    ){

        val updatedMessage = messageDataSource.updateMessageStatus(timeStamp)

        updatedMessage?.let {

            val jsonMessage = json.encodeToString<ServerResponse.MessageResponse>(
                it.toMessageResponse()
            )

            users[it.userId]?.socket?.send(Frame.Text(jsonMessage))

            println("!!!!!!!!!!!!!!!RETURNIN TO ${it.userId} : ${it.message}")


        }

    }

    suspend fun sendMessage(
        userFrom : UserDb,
        userTo : Long,
        message : String,
        timeStamp : Long
    ){

        val composeMessage = Message(
            timestamp = timeStamp,
            username = userFrom.username,
            message = message,
            userId = userFrom.id,
            receiver = userTo,
            status = MESSAGE_SENT
            )


        messageDataSource.insertMessage(composeMessage)

//        messageDataSource.insertPersonalMessage(userFrom.id, userTo.id, composeMessage)

        val jsonMessage = json.encodeToString(composeMessage.toMessageResponse())

        users[userFrom.id]?.socket?.send(Frame.Text(jsonMessage))

        userDataSource.findUserById(userTo)?.let {
            users[it.id]?.socket?.send(Frame.Text(jsonMessage))
        }

    }

    suspend fun handleVoiceCallRequest(
        sender : Long,
        request : IncomingRequest.VoiceCallRequest
    ){

        val receiver = users[request.receiver]

        when(request.status){

            VOICE_CALL_ACCEPT -> {
            // IN THIS CASE NOTIFY RECEIVER OF ACCEPTANCE
            // UPDATE BOTH USERS WITH IN VOICE CALL FIELD


               receiver?.let {

                   val jsonResponse = json.encodeToString(
                       ServerResponse.VoiceCallResponse(
                           type = VOICE_CALL_RESPONSE,
                           sender,
                           VOICE_CALL_ACCEPT
                       )
                   )

                   users[request.receiver]?.socket?.send(Frame.Text(jsonResponse))

                   users[request.receiver] = users[request.receiver]!!.copy(inVoiceCall = sender)

                   users[sender] = users[sender]!!.copy(inVoiceCall = request.receiver)

               }
            }

            VOICE_CALL_DECLINE -> {
            // NOTIFY RECEIVER OF DECLINE
            // CLEAR IN VOICE CALL FIELD FOR BOTH USERS

                val jsonResponse = json.encodeToString(
                    ServerResponse.VoiceCallResponse(
                        type = VOICE_CALL_RESPONSE,
                        sender,
                        VOICE_CALL_DECLINE
                    )
                )

                users[request.receiver]?.socket?.send(Frame.Text(jsonResponse))

                users[request.receiver]?.let {
                    users[request.receiver] = users[request.receiver]!!.copy(inVoiceCall = null)
                }

                users[sender]?.let {
                    users[sender] = users[sender]!!.copy(inVoiceCall = null)
                }
            }

            VOICE_CALL_REQUEST -> {

            // CHECK IF REQUEST IS VALID
            // IF YES NOTIFY RECEIVER ELSE NOTIFY SENDER

                var result = true

                if(receiver == null || receiver.inVoiceCall != null)
                    result = false

                if(result){

                    val jsonResponse = json.encodeToString(
                        ServerResponse.VoiceCallResponse(
                            type = VOICE_CALL_RESPONSE,
                           sender, VOICE_CALL_REQUEST
                        )
                    )
                    users[request.receiver]?.socket?.send(jsonResponse)
                } else {

                    val jsonResponse = json.encodeToString(
                        ServerResponse.VoiceCallResponse(
                            type = VOICE_CALL_RESPONSE,
                            request.receiver,
                            VOICE_CALL_DECLINE
                        )
                    )

                    users[sender]?.socket?.send(jsonResponse)

                }
            }
        }
    }

//    private suspend fun endVoiceCall(sender : Long, receiver: Long?){
//
//    }


    suspend fun sendVoiceCallChunks(
        sender : Long,
        buffer : ByteArray
    ){

//        val receiver = users[sender]?.inVoiceCall

        val frame = Frame.Binary(true, buffer)

        users[sender]?.socket?.send(frame)

//        receiver?.let {
//            users[receiver]?.socket?.send(frame)
//        } ?: run {
//        }
    }

    private suspend fun sendUsersList(){

        val list = userDataSource.getAllUsers().map {
            if(users.containsKey(it.userId)){
//                println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!FOR : ${it.userName}")
//                println("!!!!!!!!!!!!!!!!!!!!!!!!!UPDATING TO TRUE")
                it.copy(isOnline = true)
            }
            else it
        }

        val jsonMessage = json.encodeToString<ServerResponse.UsersList>(
            ServerResponse.UsersList(type = "users", users = list)
        )

//        println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!SENDING: $jsonMessage")
        users.forEach {
            try{


                it.value.socket.send(Frame.Text(jsonMessage))
            } catch (e : Exception){
                println(e.printStackTrace())
                println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!SESSION ERROR???")
                tryDisconnectUser(it.key)
            }
        }
    }


    suspend fun refreshMessages(sender: Long, receiver: Long) : List<ServerResponse.MessageResponse>{
        return messageDataSource.refreshMessages(
            receiver = receiver,
            sender = sender
        )
    }

    suspend fun getNewMessagesPage(sender: Long, receiver: Long, fromTime : Long) : List<ServerResponse.MessageResponse>{
        return messageDataSource.getNewMessagesPage(
            receiver = receiver,
            sender = sender,
            fromTime = fromTime
        )
    }

    suspend fun getOldMessagesPage(sender: Long, receiver: Long, untilTime : Long) : List<ServerResponse.MessageResponse>{
        return messageDataSource.getOldMessagesPage(
            receiver = receiver,
            sender = sender,
            untilTime = untilTime
        )
    }

    suspend fun updateProfilePic(userId: Long, url : String){
        userDataSource.updateProfilePicUrl(userId, url)
    }

    suspend fun getProfilePicUrl(userId : Long) : String? {
        return userDataSource.getProfilePicUrl(userId)
    }

    suspend fun tryDisconnectUser(userId : Long){
        users[userId]?.socket?.close()
        if(users.containsKey(userId)) users.remove(userId)

    }
}