package com.ktorchat.routes


import com.ktorchat.Constants.MESSAGE_READ
import com.ktorchat.Constants.USER_ID
import com.ktorchat.data.UserDataSource
import com.ktorchat.data.model.requests.MessagesPageRequest
import com.ktorchat.data.model.requests.IncomingRequest
import com.ktorchat.room.MemberAlreadyExistsException
import com.ktorchat.room.RoomController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json


fun Route.chatSocket(
    roomController: RoomController,
    userDataSource: UserDataSource
){

    authenticate {

        webSocket("/chat-socket"){

            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim(USER_ID, Long::class)

            val user = userDataSource.findUserById(userId ?: -1)

//            val session = call.sessions.get<ChatSession>()


            if(user == null){

                close()
                return@webSocket
            }

            try {

                val socket = this@webSocket

                roomController.join(
                    userId = user.id,
                    socket = socket
                )

//                CoroutineScope(Dispatchers.IO).launch {
//                    while(true){
//                        delay(3000)
//                        send(Frame.Text("TEST"))
//                    }
//                }

                incoming.consumeEach {frame ->

                    if(frame is Frame.Text){

                        val message = Json.decodeFromString<IncomingRequest>(frame.readText())

                        if(message is IncomingRequest.IncomingMessage){
                            if(message.status == MESSAGE_READ)
                                roomController.updateMessageStatus(message.timeStamp)
                            else {
                                roomController.sendMessage(
                                    userFrom = user,
                                    userTo = message.receiver,
                                    message = message.message,
                                    timeStamp = message.timeStamp
                                )
                            }
                        } else if(message is IncomingRequest.VoiceCallRequest){

                            try {
                                println("!!!!!!!!!!!!!!!!!!!!!HANDLING REQUEST")
                                roomController.handleVoiceCallRequest(user.id, message)

                            } catch (e : Exception){
                                println("!!!!!!!${e.stackTraceToString()}")
                            }


                        }


                    } else if (frame is Frame.Binary){
                        println("!!!!!!!!!!!!!!!!!!!!!GETTING CHUNK!!!")
                        try {

                            roomController.sendVoiceCallChunks(user.id, frame.data)
                        } catch (e : Exception){
                            println(e.stackTraceToString())
                        }



                    }
                }


            } catch (e : MemberAlreadyExistsException){
                call.respond(HttpStatusCode.Conflict)
            } catch (e : Exception){
                e.printStackTrace()
            } finally {
                roomController.tryDisconnectUser(
                    userId = user.id)
            }
        }
    }
}

fun Route.refreshMessagesPage(
    roomController: RoomController
){

    authenticate {

        get("/refresh_messages"){

            val principal = call.principal<JWTPrincipal>()
            val senderId = principal?.getClaim(USER_ID, Long::class) ?: -1

            val receiver = call.parameters["receiver"]?.toLong()

            receiver?.let {

                roomController.refreshMessages(
                    sender = senderId,
                    receiver = receiver
                ).let { list ->
                    call.respond(HttpStatusCode.OK, list)
                }
            }
        }
    }
}

fun Route.getNewMessagesPage(
    roomController: RoomController
){

    authenticate {

        get("/new_messages_page"){

            val principal = call.principal<JWTPrincipal>()
            val senderId = principal?.getClaim(USER_ID, Long::class) ?: -1

            val request = call.receiveNullable<MessagesPageRequest>()

            request?.let {

                roomController.getNewMessagesPage(
                    sender = senderId,
                    receiver = it.receiver,
                    fromTime = it.lastMessage
                ).let { list ->
                    call.respond(HttpStatusCode.OK, list)
                }
            }
        }
    }
}

fun Route.getOldMessagesPage(
    roomController: RoomController
){

    authenticate {

        get("/old_messages_page"){

            val principal = call.principal<JWTPrincipal>()
            val senderId = principal?.getClaim(USER_ID, Long::class) ?: -1

            val request = call.receiveNullable<MessagesPageRequest>()

            request?.let {

                roomController.getOldMessagesPage(
                    sender = senderId,
                    receiver = it.receiver,
                    untilTime = it.lastMessage
                ).let { list ->
                    call.respond(HttpStatusCode.OK, list)
                }
            }
        }
    }
}