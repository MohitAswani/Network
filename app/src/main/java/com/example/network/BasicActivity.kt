package com.example.network

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.network.utilities.Constants
import com.example.network.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import java.util.*

open class BasicActivity : AppCompatActivity() {
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var documentReference:DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic)

        preferenceManager= PreferenceManager(applicationContext)

        firebaseFirestore= FirebaseFirestore.getInstance()

        documentReference=firebaseFirestore
            .collection(Constants.KEY_COLLECTIONS_USER)
            .document(preferenceManager.getString(Constants.KEY_USER_ID))

        EmojiManager.install(GoogleEmojiProvider())
    }

    override fun onResume(){
        super.onResume()
        documentReference.update(Constants.KEY_IS_ONLINE,true)
    }

    override fun onPause() {
        super.onPause()

        documentReference.update(Constants.KEY_IS_ONLINE,false,
                                 Constants.LAST_SEEN,Date())
    }
}