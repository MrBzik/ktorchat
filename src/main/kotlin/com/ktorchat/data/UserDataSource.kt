package com.ktorchat.data

import com.ktorchat.data.model.responses.ServerResponse
import com.ktorchat.data.model.entities.UserDb

interface UserDataSource {

    suspend fun createUser(user: UserDb) : Boolean

    suspend fun findUser(username : String) : UserDb?

    suspend fun findUserById(userId : Long) : UserDb?

    suspend fun getAllUsers() : List<ServerResponse.UserInfo>

    suspend fun countUsers() : Long

    suspend fun updateProfilePicUrl(userId: Long, url : String)

    suspend fun getProfilePicUrl(userId: Long) : String?

//    suspend fun migrateToNewUserDb()

}