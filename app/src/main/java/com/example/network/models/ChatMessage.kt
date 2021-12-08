package com.example.network.models

import java.util.*

data class ChatMessage(
    val senderId:String,
    val receiverId:String,
    val message:String,
    val dateTime:String,
    val dateObject:Date
)