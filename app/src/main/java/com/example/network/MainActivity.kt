package com.example.network

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import com.example.network.databinding.ActivityMainBinding
import com.example.network.phoneAuth.phonelogin
import com.example.network.utilities.Constants
import com.example.network.utilities.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import android.widget.Toast
import android.R.menu
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.lang.reflect.Field
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var mAuth: FirebaseAuth

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        preferenceManager = PreferenceManager(applicationContext)
        loadDetails()
        getToken()
        binding.menuImage.setOnClickListener { // Initializing the popup menu and giving the reference as current context
            val popupMenu = PopupMenu(this@MainActivity, binding.menuImage)

            // Inflating popup menu from popup_menu.xml file
            popupMenu.menuInflater.inflate(R.menu.main_activity_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem -> // Toast message on menu item clicked
                when (menuItem.itemId) {
                    R.id.log_out_option -> {
                        signOut()
                        val intent = Intent(this@MainActivity, phonelogin::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> true
                }
            }
            // Showing the popup menu
            popupMenu.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out_option -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
        val database = FirebaseFirestore.getInstance()

        val documentReference = database.collection(Constants.KEY_COLLECTIONS_USER)
            .document(preferenceManager.getString(Constants.KEY_USER_ID))

        documentReference.update(Constants.FCM_TOKEN, token)
            .addOnSuccessListener {
                showToast("Token updated successfully")
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

