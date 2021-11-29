package com.example.network.splashscreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.network.MainActivity
import com.example.network.R
import com.example.network.phoneAuth.phonelogin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.*

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity()
{
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        mAuth= FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({
            updateUI(currentUser)
        },2000)
    }

    private fun updateUI(user: FirebaseUser? = mAuth.currentUser) {
        if (user == null) {
            val intent = Intent(this@SplashScreen, phonelogin::class.java)
            startActivity(intent)
        }
        else
        {
            val intent = Intent(this@SplashScreen, MainActivity::class.java)
            startActivity(intent)
        }
    }
}