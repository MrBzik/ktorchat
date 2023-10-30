package com.ktorchat.data

import com.ktorchat.data.model.responses.ServerResponse
import com.ktorchat.data.model.entities.UserDb
import com.ktorchat.data.model.mappers.toUserInfo
import com.mongodb.client.model.Updates
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class UserDataSourceImpl (
    db : CoroutineDatabase
) : UserDataSource {

    private val usersDb = db.getCollection<UserDb>()

    override suspend fun countUsers(): Long {
        return usersDb.countDocuments()
    }


    override suspend fun getAllUsers(): List<ServerResponse.UserInfo> {
        return usersDb.find()
            .descendingSort(UserDb::username)
            .toList().map {
                it.toUserInfo()
            }
    }

    override suspend fun createUser(user: UserDb): Boolean {

        return usersDb.insertOne(user).wasAcknowledged()

    }

    override suspend fun findUserById(userId: Long): UserDb? {
        return usersDb.findOne(UserDb::id eq userId)
    }

    override suspend fun findUser(username : String): UserDb? {
        return usersDb.findOne(UserDb::username eq username)
    }


    override suspend fun updateProfilePicUrl(userId: Long, url: String) {
        usersDb.updateOne(
            UserDb::id eq userId,
            Updates.set(UserDb::profilePic.name, url)
        )
    }

    override suspend fun getProfilePicUrl(userId: Long): String? {
        return usersDb.findOne(
            UserDb::id eq userId
        )?.profilePic
    }
}