package com.ktorchat.room

class MemberAlreadyExistsException : Exception(
    "Member with such name already exists in the room"
)