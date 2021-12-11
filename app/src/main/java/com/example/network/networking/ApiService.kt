package com.example.network.networking

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST


interface ApiService {
    // Request method and URL specified in the annotation
    @POST("send")
    fun sendMessage(
        @HeaderMap headers:HashMap<String,String>,
        @Body message: String
    ) : Call<String>
}