package com.example.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.network.adapters.ChatAdapter
import com.example.network.databinding.ActivityChatBinding
import com.example.network.models.ChatMessage
import com.example.network.models.Users
import com.example.network.phoneAuth.ChkOTP.Companion.TAG
import com.example.network.utilities.Constants
import com.example.network.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_chat.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ChatActivity : BasicActivity() {

    private lateinit var binding: ActivityChatBinding

    private lateinit var rUser: Users

    private lateinit var chatMessages: ArrayList<ChatMessage>

    private lateinit var chatAdapter: ChatAdapter

    private lateinit var preferenceManager: PreferenceManager

    private lateinit var database: FirebaseFirestore

    private var conversationId: String? = null

    private var isReceiverAvailable: Boolean = false

    private var lastSeen: Date? = null

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
        chatMessages = ArrayList()
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
            Constants.KEY_COLLECTION_CHATS
        ).add(message)

        if (conversationId != null) {
            updateConversation(binding.inputText.text.toString())
        } else {
            val conversation = HashMap<String, Any>()
            conversation[Constants.KEY_SENDER_ID] =
                preferenceManager.getString(Constants.KEY_USER_ID)
            conversation[Constants.KEY_SENDER_NAME] =
                preferenceManager.getString(Constants.KEY_NAME)
            conversation[Constants.KEY_SENDER_IMAGE] =
                preferenceManager.getString(Constants.KEY_IMAGE)
            conversation[Constants.KEY_RECEIVER_ID] = rUser.id
            conversation[Constants.KEY_RECEIVER_NAME] = rUser.name
            conversation[Constants.KEY_RECEIVER_IMAGE] = rUser.image
            conversation[Constants.KEY_RECENT_MESSAGE] = binding.inputText.text.toString()
            conversation[Constants.KEY_TIMESTAMP] = Date()
            addConversation(conversation)
        }
        binding.inputText.text = null
    }

    private fun listenAvailabilityOfReceiver() {

        database.collection(Constants.KEY_COLLECTIONS_USER)
            .document(rUser.id)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen:error", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val status: Boolean? = snapshot.getBoolean(Constants.KEY_IS_ONLINE)
                    val last_seen: Date? = snapshot.getDate(Constants.LAST_SEEN)
                    if (status != null) {
                        isReceiverAvailable = status
                    }

                    if (last_seen != null) {
                        Log.d("ChatActivity", getReadableDate(last_seen))
                        lastSeen = last_seen
                    }
                }
                if (isReceiverAvailable) {
                    binding.availability.text = "Online"
                } else {
                    binding.availability.text = lastSeen?.let { getReadableDate(it) }
                }

            }
    }

    private fun listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHATS)
            .whereEqualTo(
                Constants.KEY_SENDER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .whereEqualTo(Constants.KEY_RECEIVER_ID, rUser.id)
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
                if (conversationId == null) {
                    checkForConversation()
                }
            }

        database.collection(Constants.KEY_COLLECTION_CHATS)
            .whereEqualTo(Constants.KEY_SENDER_ID, rUser.id)
            .whereEqualTo(
                Constants.KEY_RECEIVER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
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
                if (conversationId == null) {
                    checkForConversation()
                }
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
            if (!binding.inputText.text.isNullOrBlank())
                sendMessage()
        }
    }

    private fun getUserImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun getReadableDate(date: Date): String {
        return if (date.date == Date().date)
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        else
            SimpleDateFormat("dd:mm:yyyy", Locale.getDefault()).format(date)
    }

    private fun addConversation(conversation: HashMap<String, Any>) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
            .add(conversation)
            .addOnSuccessListener {
                conversationId = it.id
            }

    }

    private fun updateConversation(message: String) {
        conversationId?.let {
            database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .document(it)
        }?.update(
            Constants.KEY_RECENT_MESSAGE, message,
            Constants.KEY_TIMESTAMP, Date()
        )
    }

    private fun checkForConversation() {
        if (chatMessages.size > 0) {
            checkForConversationRemotely(
                preferenceManager.getString(Constants.KEY_USER_ID),
                rUser.id
            )
            checkForConversationRemotely(
                rUser.id,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
        }
    }


    private fun checkForConversationRemotely(senderId: String, receiverId: String) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result != null && it.result!!.documents.size > 0) {
                    conversationId = it.result!!.documents[0].id
                }
            }

    }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
    }


}