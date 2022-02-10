package com.example.network

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import com.example.network.adapters.ViewPagerFragmentAdapter
import com.example.network.databinding.ActivityMainBinding
import com.example.network.models.Conversation
import com.example.network.phoneAuth.ChkOTP
import com.example.network.phoneAuth.phonelogin
import com.example.network.utilities.Constants
import com.example.network.utilities.PreferenceManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.scottyab.aescrypt.AESCrypt
import java.util.*
import kotlin.collections.HashMap


class MainActivity : BasicActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var mAuth: FirebaseAuth

    private lateinit var preferenceManager: PreferenceManager

    private lateinit var viewPagerFragmentAdapter: ViewPagerFragmentAdapter

    private val titles = arrayOf(R.string.tab_chat, R.string.tab_status)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        preferenceManager = PreferenceManager(applicationContext)
        loadDetails()
        getToken()
        setListeners()
        binding.menuImage.setOnClickListener { // Initializing the popup menu and giving the reference as current context
            val popupMenu = PopupMenu(this@MainActivity, binding.menuImage)

            // Inflating popup menu from popup_menu.xml file
            popupMenu.menuInflater.inflate(R.menu.main_activity_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem -> // Toast message on menu item clicked
                when (menuItem.itemId) {
                    R.id.log_out_option -> {
                        signOut()
                        true
                    }
                    else -> true
                }
            }
            // Showing the popup menu
            popupMenu.show()

        }

        viewPagerFragmentAdapter = ViewPagerFragmentAdapter(this)
        binding.viewPager.adapter = viewPagerFragmentAdapter
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab: TabLayout.Tab, i: Int ->
            tab.setText(
                titles[i]
            )
        }.attach()

        setKeys()

    }

    private fun setKeys() {
        val database = FirebaseFirestore.getInstance()

        database.collection("keys").whereEqualTo(
            "user1",
            preferenceManager.getString(Constants.KEY_USER_ID)
        ).addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(ChkOTP.TAG, "Listen:error", e)
                return@addSnapshotListener
            }
            if (snapshots != null) {
                for (dc in snapshots.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        preferenceManager.putString(
                            dc.document.getString("user2")!!,
                            dc.document.getString("key")!!
                        )
                    } else if (dc.type == DocumentChange.Type.MODIFIED) {
                        preferenceManager.putString(
                            dc.document.getString("user2")!!,
                            dc.document.getString("key")!!
                        )
                    }
                }
            }
        }

        database.collection("keys").whereEqualTo(
            "user2",
            preferenceManager.getString(Constants.KEY_USER_ID)
        ).addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(ChkOTP.TAG, "Listen:error", e)
                return@addSnapshotListener
            }
            if (snapshots != null) {
                for (dc in snapshots.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        preferenceManager.putString(
                            dc.document.getString("user1")!!,
                            dc.document.getString("key")!!
                        )
                    } else if (dc.type == DocumentChange.Type.MODIFIED) {
                        preferenceManager.putString(
                            dc.document.getString("user1")!!,
                            dc.document.getString("key")!!
                        )
                    }
                }
            }
        }
    }

    private fun setListeners() {
        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(this@MainActivity, UserActivity::class.java))
        }
    }

    private fun loadDetails() {
        binding.userName.text = preferenceManager.getString(Constants.KEY_NAME)
        val bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.userImage.setImageBitmap(bitmap)
    }

    private fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
    }

    private fun updateToken(token: String) {
        preferenceManager.putString(Constants.FCM_TOKEN, token)
        val database = FirebaseFirestore.getInstance()

        val documentReference = database.collection(Constants.KEY_COLLECTIONS_USER)
            .document(preferenceManager.getString(Constants.KEY_USER_ID))

        documentReference.update(Constants.FCM_TOKEN, token)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
                showToast("Token not updated")
            }
    }

    private fun signOut() {
        val database = FirebaseFirestore.getInstance()

        val documentReference = database.collection(Constants.KEY_COLLECTIONS_USER).document(
            preferenceManager.getString(Constants.KEY_USER_ID)
        )

        val h = HashMap<String, Any>()
        h[Constants.FCM_TOKEN] = FieldValue.delete()
        documentReference.update(h)
            .addOnSuccessListener {
                preferenceManager.clear()
                mAuth.signOut()
                val intent = Intent(applicationContext, phonelogin::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                showToast("Sign out failed")
            }
    }

}

