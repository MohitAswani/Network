package com.example.network.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.network.databinding.ItemContainerRecentBinding
import com.example.network.listeners.ConversationListener
import com.example.network.models.Conversation
import com.example.network.models.Users
import java.text.SimpleDateFormat
import java.util.*


class RecentConversationAdapter(private val recentConversations: List<Conversation>,val senderId:String,val conversationListener: ConversationListener):RecyclerView.Adapter<RecentConversationAdapter.ConversationViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {

        val itemContainerRecentBinding= ItemContainerRecentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationViewHolder(itemContainerRecentBinding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.setData(recentConversations[position])
    }

    override fun getItemCount(): Int {
        return recentConversations.size
    }

    inner class ConversationViewHolder(val binding: ItemContainerRecentBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun setData(recentConversation: Conversation){
            binding.textName.text = recentConversation.conversationName
            binding.imageProfile.setImageBitmap(getUserImage(recentConversation.conversationImage))
            binding.textRecent.text=recentConversation.last_message
            binding.recentTime.text=getReadableDate(recentConversation.dateObject)
            binding.root.setOnClickListener{
                val user=Users(
                    name =recentConversation.conversationName,
                    id =recentConversation.conversationId,
                    image = recentConversation.conversationImage,
                    phoneNumber = null,
                    token = null,
                    status = ""
                )
                conversationListener.onConversationClicked(user)
            }
        }
    }

    private fun getUserImage(encodedImage:String) :Bitmap
    {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return  BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun getReadableDate(date: Date): String {
        return if(date.date==Date().date)
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        else
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
    }
}