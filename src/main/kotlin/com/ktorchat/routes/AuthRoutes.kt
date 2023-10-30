package com.ktorchat.routes

import com.ktorchat.Constants.USER_ID
import com.ktorchat.data.UserDataSource
import com.ktorchat.data.auth.AuthRequest
import com.ktorchat.data.auth.AuthResponse
import com.ktorchat.data.model.entities.UserDb
import com.ktorchat.security.hashing.HashingService
import com.ktorchat.security.hashing.SaltedHash
import com.ktorchat.security.token.TokenClaim
import com.ktorchat.security.token.TokenConfig
import com.ktorchat.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUp(
    hashingService : HashingService,
    userDataSource: UserDataSource
){

    post("signup"){

        val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val isFieldBlank = request.username.isBlank() || request.password.isBlank()
        val alreadyExists = userDataSource.findUser(request.username) != null

        if(isFieldBlank || alreadyExists){
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val hash = hashingService.generateSaltedHash(request.password)

        val id = userDataSource.countUsers() + 1

        val newUser = UserDb(
            username = request.username,
            password = hash.hash,
            salt = hash.salt,
            id = id
        )

        val success = userDataSource.createUser(newUser)

        call.respond(
            if(success) HttpStatusCode.OK
            else HttpStatusCode.BadRequest
        )

    }
}


fun Route.signIn(
    hashingService : HashingService,
    userDataSource: UserDataSource,
    tokenService: TokenService,
    tokenConfig: TokenConfig
){

    post("signin"){
        val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.findUser(request.username)

        user?.let {

            val isValid = hashingService.verify(
                value = request.password,
                saltedHash = SaltedHash(
                    hash = user.password,
                    salt = user.salt
                )
            )

            if(isValid){

                val token = tokenService.generate(
                    tokenConfig = tokenConfig,
                    TokenClaim(
                        name = USER_ID,
                        value = user.id
                    )
                )

                call.respond(
                    status = HttpStatusCode.OK,
                    message = AuthResponse(
                        token = token,
                        userId = user.id
                    )
                )
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }


        } ?: call.respond(HttpStatusCode.BadRequest)
    }
}

fun Route.authenticate(){

    authenticate {

        get("authenticate"){

            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim(USER_ID, Long::class)

            call.respond(
                status = HttpStatusCode.OK,
                message = userId ?: -1
            )
        }
    }
}