package com.ktorchat.koin

import com.ktorchat.data.MessageDataSource
import com.ktorchat.data.MessageDataSourceImpl
import com.ktorchat.data.UserDataSource
import com.ktorchat.data.UserDataSourceImpl
import com.ktorchat.room.RoomController
import com.ktorchat.security.hashing.HashingService
import com.ktorchat.security.hashing.SHA256HashingService
import com.ktorchat.security.token.JwtTokenService
import com.ktorchat.security.token.TokenService
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val mainModule = module {

    single {

       KMongo.createClient()
            .coroutine
            .getDatabase("message_dp")
    }


    single<UserDataSource> {
        UserDataSourceImpl(get())
    }


    single<MessageDataSource> {
        MessageDataSourceImpl(get())
    }


    single<TokenService> {
        JwtTokenService()
    }

    single<HashingService> {
        SHA256HashingService()
    }

    single {
        RoomController(get(), get())
    }

}