package com.ktorchat

import com.ktorchat.koin.mainModule
import com.ktorchat.plugins.*
import com.ktorchat.security.token.TokenConfig
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}


fun Application.module() {

    install(Koin){
        modules(mainModule)
    }

    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 2 * 1000L * 60 * 60 * 24,
        secret = System.getenv("JWT_SECRET")
    )

    configureMonitoring()
    configureSerialization()
    configureSockets()
    configureSecurity(tokenConfig)
    configureRouting(tokenConfig)



}
