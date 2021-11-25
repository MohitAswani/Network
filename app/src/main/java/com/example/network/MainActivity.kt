package com.example.network

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.network.databinding.ActivityMainBinding
import com.example.network.login.phonelogin
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding

    private lateinit var mAuth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth= FirebaseAuth.getInstance()

        binding.logOutButton.setOnClickListener{
            mAuth.signOut()
            val intent= Intent(this@MainActivity, phonelogin::class.java)
            startActivity(intent)
        }
    }
}