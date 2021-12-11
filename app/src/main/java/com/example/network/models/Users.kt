package com.example.network.models

import java.io.Serializable

data class Users(
    val name:String,
    val image: String?,
    val phoneNumber:String?,
    var token:String?,
    var id:String,
    val status: String?
) : Serializable