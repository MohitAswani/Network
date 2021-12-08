package com.example.network.models

import java.io.Serializable

data class Users ( val name:String,
                   val image:String,
                   val phoneNumber:String?,
                   val token:String?,
                   val id:String
                   ) : Serializable