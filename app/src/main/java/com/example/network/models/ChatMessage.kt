package com.example.network.models

import java.util.*

data class ChatMessage(
    val senderId:String,
    val receiverId:String,
    var message:String?,
    val dateTime:String,
    val dateObject:Date,
    var file:String,
    val fileType:String
)