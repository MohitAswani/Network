package com.example.network

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.network.adapters.ChatAdapter
import com.example.network.databinding.ActivityChatBinding
import com.example.network.models.ChatMessage
import com.example.network.models.Users
import com.example.network.phoneAuth.ChkOTP.Companion.TAG
import com.example.network.signup.SignUp
import com.example.network.utilities.Constants
import com.example.network.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding

    private lateinit var rUser: Users

    private lateinit var chatMessages: java.util.ArrayList<ChatMessage>

    private lateinit var chatAdapter: ChatAdapter

    private lateinit var preferenceManager: PreferenceManager

    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        clickListeners()
        setUserValues()
        init()
        listenMessages()
    }

    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessages = java.util.ArrayList()
        chatAdapter = ChatAdapter(
            chatMessages,
            preferenceManager.getString(Constants.KEY_USER_ID)
        )
        binding.chatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage() {
        val message = HashMap<String, Any>()
        Log.i("YOLO", "id not found")
        message[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID)
        message[Constants.KEY_RECEIVER_ID] = rUser.id
        message[Constants.KEY_MESSAGE] = binding.inputText.text.toString()
        message[Constants.KEY_TIMESTAMP] = Date()
        Log.d("YOLO", "id found")

        database.collection(
            Constants.KEY_COLLECTIONS_USER + "/" +
                    preferenceManager.getString(Constants.KEY_USER_ID) + "/" +
                    Constants.KEY_SUB_COLLECTION_FRIENDS + "/" +
                    rUser.id + "/" + Constants.KEY_SUB_COLLECTION_CHATS
        )
            .add(message)

        database.collection(
            Constants.KEY_COLLECTIONS_USER + "/" +
                    rUser.id + "/" +
                    Constants.KEY_SUB_COLLECTION_FRIENDS + "/" +
                    preferenceManager.getString(Constants.KEY_USER_ID) + "/" + Constants.KEY_SUB_COLLECTION_CHATS
        )
            .add(message)

        binding.inputText.text = null
    }

    private fun listenMessages() {
        database.collection(
            Constants.KEY_COLLECTIONS_USER + "/" +
                    preferenceManager.getString(Constants.KEY_USER_ID) + "/" +
                    Constants.KEY_SUB_COLLECTION_FRIENDS + "/" +
                    rUser.id + "/" + Constants.KEY_SUB_COLLECTION_CHATS
        )
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen:error", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val count = chatMessages.size
                    for (dc in snapshots.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val message = ChatMessage(
                                    senderId = dc.document.getString(Constants.KEY_SENDER_ID)!!,
                                    receiverId = dc.document.getString(Constants.KEY_RECEIVER_ID)!!,
                                    message = dc.document.getString(Constants.KEY_MESSAGE)!!,
                                    dateTime = getReadableDate(dc.document.getDate(Constants.KEY_TIMESTAMP)!!),
                                    dateObject = dc.document.getDate(Constants.KEY_TIMESTAMP)!!
                                )
                                chatMessages.add(message)
                            }
                            else -> {}
                        }
                    }
                    chatMessages.sortBy { it.dateObject }
                    if (count == 0) {
                        chatAdapter.notifyDataSetChanged()
                    } else {
                        chatAdapter.notifyItemRangeInserted(count, chatMessages.size - count)
                        binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
                    }
                    binding.chatRecyclerView.visibility = View.VISIBLE
                }
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun setUserValues() {
        rUser = intent.getSerializableExtra(Constants.KEY_USER) as Users
        binding.textName.text = rUser.name
        binding.imageProfile.setImageBitmap(getUserImage(rUser.image))
    }

    private fun showToast(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    private fun clickListeners() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun getUserImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun getReadableDate(date: Date): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
    }
}