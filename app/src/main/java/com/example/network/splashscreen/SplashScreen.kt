package com.example.network.splashscreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.network.MainActivity
import com.example.network.R
import com.example.network.phoneAuth.phonelogin
import com.example.network.utilities.Constants
import com.example.network.utilities.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.*

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity()
{
    private lateinit var mAuth: FirebaseAuth

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        mAuth= FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        supportActionBar?.hide()

        preferenceManager= PreferenceManager(applicationContext)

        Handler(Looper.getMainLooper()).postDelayed({
            updateUI(currentUser)
        },2000)
    }

    private fun updateUI(user: FirebaseUser? = mAuth.currentUser) {
        if (user == null) {
            val intent = Intent(this@SplashScreen, phonelogin::class.java)
            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,false)
            startActivity(intent)
            finish()
        }
        else
        {
            if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
                Log.d("SIGNED", preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN).toString())
                val intent = Intent(this@SplashScreen, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else
            {
                val intent = Intent(this@SplashScreen, phonelogin::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}