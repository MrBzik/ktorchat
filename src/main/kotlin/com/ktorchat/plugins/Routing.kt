package com.ktorchat.plugins

import com.ktorchat.data.UserDataSource
import com.ktorchat.room.RoomController
import com.ktorchat.routes.*
import com.ktorchat.security.hashing.HashingService
import com.ktorchat.security.token.TokenConfig
import com.ktorchat.security.token.TokenService
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import java.io.File

fun Application.configureRouting(
    tokenConfig: TokenConfig
) {

    val roomController by inject<RoomController>()
    val hashingService by inject<HashingService>()
    val tokenService by inject<TokenService>()
    val userDataSource by inject<UserDataSource>()

    install(Routing){

        signUp(hashingService, userDataSource)
        signIn(hashingService, userDataSource, tokenService, tokenConfig)
        authenticate()
        chatSocket(roomController, userDataSource)
        refreshMessagesPage(roomController)
        getNewMessagesPage(roomController)
        getOldMessagesPage(roomController)
        getProfileBitmapFromClient(roomController)
        sendProfileBitmapUriToClient(roomController)

        staticFiles("/", File("files"))
    }
}
