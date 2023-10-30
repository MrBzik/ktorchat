package com.ktorchat.routes

import com.ktorchat.Constants
import com.ktorchat.Constants.BASE_URL
import com.ktorchat.room.RoomController
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.getProfileBitmapFromClient(
    roomController: RoomController
){

    authenticate {

        post("/send_profile_pic_bitmap"){

            val path = "C:\\Users\\Niccolò\\Desktop\\ktorchat\\files\\profile\\"
            val principal = call.principal<JWTPrincipal>()
            val senderId = principal?.getClaim(Constants.USER_ID, Long::class) ?: -1
            val request = call.receiveMultipart()
            request.forEachPart { data ->
                if(data is PartData.FileItem){
                    data.apply {
                        val fileBytes = streamProvider().readBytes()
                        val fileName = "$senderId.png"
                        val fullPath = path + fileName
                        File(fullPath).writeBytes(fileBytes)
                        val fileUrl = BASE_URL + "profile\\" + fileName
                        roomController.updateProfilePic(senderId, fileUrl)
                    }
                }
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.sendProfileBitmapUriToClient(
    roomController: RoomController
){

    authenticate {

        get("/get_profile_pic_uri"){

            val principal = call.principal<JWTPrincipal>()
            val senderId = principal?.getClaim(Constants.USER_ID, Long::class) ?: -1

            roomController.getProfilePicUrl(senderId)?.let {url ->
                call.respond(HttpStatusCode.OK, url)
            }
        }
    }
}