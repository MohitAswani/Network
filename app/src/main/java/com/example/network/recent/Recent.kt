package com.example.network.recent

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.network.ChatActivity
import com.example.network.R
import com.example.network.adapters.RecentConversationAdapter
import com.example.network.databinding.RecentFragmentBinding
import com.example.network.listeners.ConversationListener
import com.example.network.models.Conversation
import com.example.network.models.Users
import com.example.network.phoneAuth.ChkOTP
import com.example.network.utilities.Constants
import com.example.network.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.scottyab.aescrypt.AESCrypt
import java.util.*

class Recent : Fragment(), ConversationListener {


    private lateinit var viewModel: RecentViewModel

    private lateinit var binding: RecentFragmentBinding

    private lateinit var preferenceManager: PreferenceManager

    private lateinit var recentMessages: ArrayList<Conversation>

    private lateinit var recentConversationAdapter: RecentConversationAdapter

    private lateinit var database: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.recent_fragment, container, false)
        init()
        listenConversations()
        return binding.root
    }

    private fun init() {
        preferenceManager = context?.let { PreferenceManager(it.applicationContext) }!!
        recentMessages = ArrayList()
        recentConversationAdapter = RecentConversationAdapter(
            recentMessages,
            preferenceManager.getString(Constants.KEY_USER_ID),
            this
        )
        binding.recentRecyclerView.adapter = recentConversationAdapter
        database = FirebaseFirestore.getInstance()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
            .whereEqualTo(
                Constants.KEY_SENDER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(ChkOTP.TAG, "Listen:error", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    for (dc in snapshots.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val senderId = dc.document.getString(Constants.KEY_SENDER_ID)!!
                            val receiverId = dc.document.getString(Constants.KEY_RECEIVER_ID)!!
                            val conversationImage: String
                            val conversationName: String
                            val conversationId: String
                            if (preferenceManager.getString(Constants.KEY_USER_ID) == senderId) {
                                conversationId =
                                    dc.document.getString(Constants.KEY_RECEIVER_ID)!!
                                conversationImage =
                                    dc.document.getString(Constants.KEY_RECEIVER_IMAGE)!!
                                conversationName =
                                    dc.document.getString(Constants.KEY_RECEIVER_NAME)!!
                            } else {
                                conversationId =
                                    dc.document.getString(Constants.KEY_SENDER_ID)!!
                                conversationImage =
                                    dc.document.getString(Constants.KEY_SENDER_IMAGE)!!
                                conversationName =
                                    dc.document.getString(Constants.KEY_SENDER_NAME)!!
                            }
                            val conversationMessage: String=
                                AESCrypt.decrypt(preferenceManager.getString(receiverId),dc.document.getString(Constants.KEY_RECENT_MESSAGE)!!)
                            val dateObject: Date =
                                dc.document.getDate(Constants.KEY_TIMESTAMP)!!
                            val conversation = Conversation(
                                senderId = senderId,
                                receiverId = receiverId,
                                conversationId = conversationId,
                                conversationImage = conversationImage,
                                conversationName = conversationName,
                                last_message = conversationMessage,
                                dateObject = dateObject
                            )
                            recentMessages.add(conversation)
                        } else if (dc.type == DocumentChange.Type.MODIFIED) {
                            for (i in 0..recentMessages.size) {
                                val senderId = dc.document.getString(Constants.KEY_SENDER_ID)
                                val receiverId =
                                    dc.document.getString(Constants.KEY_RECEIVER_ID)
                                if (recentMessages[i].senderId == senderId && recentMessages[i].receiverId == receiverId) {
                                        recentMessages[i].last_message =
                                            AESCrypt.decrypt(preferenceManager.getString(receiverId),dc.document.getString(Constants.KEY_RECENT_MESSAGE)!!)
                                    recentMessages[i].dateObject =
                                        dc.document.getDate(Constants.KEY_TIMESTAMP)!!
                                    break;
                                }

                            }
                        }
                    }
                    recentMessages.sortByDescending { it.dateObject }
                    recentConversationAdapter.notifyDataSetChanged()
                    binding.recentRecyclerView.smoothScrollToPosition(0)
                    binding.recentRecyclerView.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
                binding.progressBar.visibility = View.GONE
            }


        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
            .whereEqualTo(
                Constants.KEY_RECEIVER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(ChkOTP.TAG, "Listen:error", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    for (dc in snapshots.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val senderId = dc.document.getString(Constants.KEY_SENDER_ID)!!
                            val receiverId = dc.document.getString(Constants.KEY_RECEIVER_ID)!!
                            val conversationImage: String
                            val conversationName: String
                            val conversationId: String
                            if (preferenceManager.getString(Constants.KEY_USER_ID) == senderId) {
                                conversationId =
                                    dc.document.getString(Constants.KEY_RECEIVER_ID)!!
                                conversationImage =
                                    dc.document.getString(Constants.KEY_RECEIVER_IMAGE)!!
                                conversationName =
                                    dc.document.getString(Constants.KEY_RECEIVER_NAME)!!
                            } else {
                                conversationId =
                                    dc.document.getString(Constants.KEY_SENDER_ID)!!
                                conversationImage =
                                    dc.document.getString(Constants.KEY_SENDER_IMAGE)!!
                                conversationName =
                                    dc.document.getString(Constants.KEY_SENDER_NAME)!!

                                if(preferenceManager.getString(senderId)=="null")
                                    preferenceManager.putString(senderId,dc.document.getString(Constants.KEY_ENCRYPT_KEY)!!)

                                val updates = hashMapOf<String, Any>(
                                    Constants.KEY_ENCRYPT_KEY to FieldValue.delete()
                                )
                                database.collection(Constants.KEY_COLLECTION_CONVERSATION).document(dc.document.id).update(updates)
                            }
                            val conversationMessage: String =
                                AESCrypt.decrypt(preferenceManager.getString(senderId),dc.document.getString(Constants.KEY_RECENT_MESSAGE)!!)
                            val dateObject: Date =
                                dc.document.getDate(Constants.KEY_TIMESTAMP)!!
                            val conversation = Conversation(
                                senderId = senderId,
                                receiverId = receiverId,
                                conversationId = conversationId,
                                conversationImage = conversationImage,
                                conversationName = conversationName,
                                last_message = conversationMessage,
                                dateObject = dateObject
                            )
                            recentMessages.add(conversation)
                        } else if (dc.type == DocumentChange.Type.MODIFIED) {
                            for (i in 0..recentMessages.size) {
                                val senderId = dc.document.getString(Constants.KEY_SENDER_ID)
                                val receiverId = dc.document.getString(Constants.KEY_RECEIVER_ID)
                                val conversationName: String
                                if (recentMessages[i].senderId == senderId && recentMessages[i].receiverId == receiverId) {
                                    recentMessages[i].last_message =
                                        AESCrypt.decrypt(preferenceManager.getString(senderId),dc.document.getString(Constants.KEY_RECENT_MESSAGE)!!)
                                    recentMessages[i].dateObject =
                                        dc.document.getDate(Constants.KEY_TIMESTAMP)!!
                                    break;
                                }
                            }
                        }
                    }
                    recentMessages.sortByDescending { it.dateObject }
                    recentConversationAdapter.notifyDataSetChanged()
                    binding.recentRecyclerView.smoothScrollToPosition(0)
                    binding.recentRecyclerView.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun showErrorMessage() {
        binding.textErrorMessage.text = "No user available"
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean) {
        when (isLoading) {
            true -> binding.progressBar.visibility = View.VISIBLE
            else -> binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onConversationClicked(user: Users) {
        val intent = Intent(context?.applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
    }


}