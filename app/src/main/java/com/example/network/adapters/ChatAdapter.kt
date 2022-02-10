package com.example.network.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.network.databinding.*
import com.example.network.listeners.FileListener
import com.example.network.listeners.ImageListener
import com.example.network.models.ChatMessage
import com.example.network.utilities.Constants

class ChatAdapter(
    private val chatMessages: java.util.ArrayList<ChatMessage>,
    private val senderId: String,
    val imageListener: ImageListener,
    val fileListener: FileListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val MESSAGE_TYPE_SENT_TEXT = 1
        const val MESSAGE_TYPE_RECEIVE_TEXT = 2
        const val MESSAGE_TYPE_SENT_IMAGE = 3
        const val MESSAGE_TYPE_RECEIVE_IMAGE = 4
        const val MESSAGE_TYPE_SENT_FILE = 5
        const val MESSAGE_TYPE_RECEIVE_FILE = 6
        const val MESSAGE_TYPE_SENT_GIF_STICKER = 7
        const val MESSAGE_TYPE_RECEIVE_GIF_STICKER = 8
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MESSAGE_TYPE_SENT_TEXT -> SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            MESSAGE_TYPE_RECEIVE_TEXT -> ReceiveMessageViewHolder(
                ItemContainerRecieveMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            MESSAGE_TYPE_SENT_IMAGE -> SentImageViewHolder(
                ItemContainerSentImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            MESSAGE_TYPE_RECEIVE_IMAGE -> ReceiveImageViewHolder(
                ItemContainerRecieveImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            MESSAGE_TYPE_SENT_FILE -> SentFileViewHolder(
                ItemContainerSentFileBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            MESSAGE_TYPE_RECEIVE_FILE -> ReceiveFileViewHolder(
                ItemContainerRecieveFileBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            MESSAGE_TYPE_SENT_GIF_STICKER -> SentGifAndStickerViewHolder(
                ItemContainerSentGifStickerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> ReceiveGifAndStickersViewHolder(
                ItemContainerRecieveGifStickerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when {
            getItemViewType(position) == MESSAGE_TYPE_SENT_TEXT -> {
                (holder as SentMessageViewHolder).setData(chatMessage = chatMessages[position])
            }
            getItemViewType(position) == MESSAGE_TYPE_SENT_IMAGE -> {
                (holder as SentImageViewHolder).setData(chatMessage = chatMessages[position])
            }
            getItemViewType(position) == MESSAGE_TYPE_SENT_FILE -> {
                (holder as SentFileViewHolder).setData(chatMessage = chatMessages[position])
            }
            getItemViewType(position) == MESSAGE_TYPE_SENT_GIF_STICKER -> {
                (holder as SentGifAndStickerViewHolder).setData(chatMessage = chatMessages[position])
            }
            getItemViewType(position) == MESSAGE_TYPE_RECEIVE_TEXT -> {
                (holder as ReceiveMessageViewHolder).setData(chatMessage = chatMessages[position])
            }
            getItemViewType(position) == MESSAGE_TYPE_RECEIVE_IMAGE -> {
                (holder as ReceiveImageViewHolder).setData(chatMessage = chatMessages[position])
            }
            getItemViewType(position) == MESSAGE_TYPE_RECEIVE_FILE -> {
                (holder as ReceiveFileViewHolder).setData(chatMessage = chatMessages[position])
            }
            else -> {
                (holder as ReceiveGifAndStickersViewHolder).setData(chatMessage = chatMessages[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun getItemViewType(position: Int): Int {
        if (chatMessages[position].senderId == senderId) {
            return when (chatMessages[position].fileType) {
                Constants.KEY_IMAGE_TYPE -> MESSAGE_TYPE_SENT_IMAGE

                Constants.KEY_FILE_TYPE -> MESSAGE_TYPE_SENT_FILE

                Constants.KEY_TEXT_TYPE -> MESSAGE_TYPE_SENT_TEXT

                else -> MESSAGE_TYPE_SENT_GIF_STICKER
            }
        } else {
            return when (chatMessages[position].fileType) {
                Constants.KEY_IMAGE_TYPE -> MESSAGE_TYPE_RECEIVE_IMAGE

                Constants.KEY_FILE_TYPE -> MESSAGE_TYPE_RECEIVE_FILE

                Constants.KEY_TEXT_TYPE -> MESSAGE_TYPE_RECEIVE_TEXT

                else -> MESSAGE_TYPE_RECEIVE_GIF_STICKER
            }
        }
    }

    inner class SentMessageViewHolder(val binding: ItemContainerSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.dataTimeText.text = chatMessage.dateTime
        }
    }

    inner class ReceiveMessageViewHolder(val binding: ItemContainerRecieveMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.dataTimeText.text = chatMessage.dateTime
        }
    }

    inner class ReceiveImageViewHolder(val binding: ItemContainerRecieveImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            binding.image.setImageBitmap(getUserImage(chatMessage.file))
            binding.dataTimeText.text = chatMessage.dateTime
            binding.image.setOnClickListener {
                imageListener.onImageClicked(chatMessage.file)
            }
        }
    }

    inner class SentImageViewHolder(val binding: ItemContainerSentImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            binding.image.setImageBitmap(getUserImage(chatMessage.file))
            binding.dataTimeText.text = chatMessage.dateTime
            binding.image.setOnClickListener {
                imageListener.onImageClicked(chatMessage.file)
            }
        }
    }

    inner class ReceiveFileViewHolder(val binding: ItemContainerRecieveFileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            binding.fileName.text = chatMessage.message
            binding.dataTimeText.text = chatMessage.dateTime
            binding.download.setOnClickListener {
                fileListener.onFileClicked(chatMessage.file, chatMessage.message!!)
            }
        }
    }

    inner class SentFileViewHolder(val binding: ItemContainerSentFileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            binding.fileName.text = chatMessage.message
            binding.dataTimeText.text = chatMessage.dateTime
            binding.download.setOnClickListener {
                fileListener.onFileClicked(chatMessage.file, chatMessage.message!!)
            }
        }
    }

    inner class ReceiveGifAndStickersViewHolder(val binding: ItemContainerRecieveGifStickerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            Glide.with(binding.root.context)
                .load(chatMessage.file?.toUri())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.image)
            binding.dataTimeText.text = chatMessage.dateTime
        }
    }


    inner class SentGifAndStickerViewHolder(val binding: ItemContainerSentGifStickerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            Glide.with(binding.root.context)
                .load(chatMessage.file.toUri())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.image)
            binding.dataTimeText.text = chatMessage.dateTime
        }
    }

    private fun getUserImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}