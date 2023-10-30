package com.ktorchat.data.model.entities

import org.bson.codecs.pojo.annotations.BsonId

data class UserDb(
    val username : String,
    val password : String,
    val salt : String,
    @BsonId
    val id: Long,
    val profilePic : String? = null
)
