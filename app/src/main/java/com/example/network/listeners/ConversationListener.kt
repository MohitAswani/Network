package com.example.network.listeners

import com.example.network.models.Users

interface ConversationListener {
    fun onConversationClicked(user: Users)
}