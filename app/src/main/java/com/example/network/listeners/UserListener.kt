package com.example.network.listeners

import com.example.network.models.Users

interface UserListener {
    fun onUserClicked(user: Users)
}