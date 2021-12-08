package com.example.network.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.network.databinding.ItemContainerRecieveMessageBinding
import com.example.network.databinding.ItemContainerSentMessageBinding
import com.example.network.models.ChatMessage

class ChatAdapter(private val chatMessages:java.util.ArrayList<ChatMessage>,private val senderId:String) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object{
        const val MESSAGE_TYPE_SENT=1
        const val MESSAGE_TYPE_RECEIVE=2
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType==MESSAGE_TYPE_SENT){
            return SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
        else
        {
            return ReceiveMessageViewHolder(
                ItemContainerRecieveMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType(position)== MESSAGE_TYPE_SENT){
            (holder as SentMessageViewHolder).setData(chatMessage = chatMessages[position])
        }
        else
        {
            (holder as ReceiveMessageViewHolder).setData(chatMessage = chatMessages[position])
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun getItemViewType(position:Int):Int{
        return when(chatMessages[position].senderId == senderId)
        {
            true -> MESSAGE_TYPE_SENT
            else -> MESSAGE_TYPE_RECEIVE
        }
    }

    inner class SentMessageViewHolder(val binding: ItemContainerSentMessageBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun setData(chatMessage: ChatMessage){
            binding.textMessage.text=chatMessage.message
            binding.dataTimeText.text=chatMessage.dateTime
        }
    }

    inner class ReceiveMessageViewHolder(val binding: ItemContainerRecieveMessageBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun setData(chatMessage: ChatMessage){
            binding.textMessage.text=chatMessage.message
            binding.dataTimeText.text=chatMessage.dateTime
        }
    }
}