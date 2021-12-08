package com.example.network.models

import java.util.*

data class Conversation(
    var conversationId:String,
    var conversationName:String,
    var conversationImage:String,
    var senderId:String,
    var receiverId:String,
    var last_message: String,
    var dateObject:Date
)
