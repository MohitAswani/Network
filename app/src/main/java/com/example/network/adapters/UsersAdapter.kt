package com.example.network.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.network.databinding.ItemContainerUserBinding
import com.example.network.listeners.UserListener
import com.example.network.models.Users

class UsersAdapter(val users: List<Users>,val userListener: UserListener):RecyclerView.Adapter<UsersAdapter.UserViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        val itemContainerUserBinding=com.example.network.databinding.ItemContainerUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(itemContainerUserBinding)
    }


    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserData(users[position])
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class UserViewHolder(val binding: ItemContainerUserBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun setUserData(user: Users){
            binding.textName.text = user.name
            binding.textStatus.text=user.status
            binding.imageProfile.setImageBitmap(user.image?.let { getUserImage(it) })
            binding.root.setOnClickListener{
                userListener.onUserClicked(user)
            }
        }
    }

    private fun getUserImage(encodedImage:String) :Bitmap
    {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return  BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}