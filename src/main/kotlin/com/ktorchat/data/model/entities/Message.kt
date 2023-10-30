package com.ktorchat.data.model.entities

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Message(
    @BsonId
    val timestamp: Long,
    val username: String,
    val userId : Long,
    val message: String,
    val receiver : Long,
    val status : Int,
)
