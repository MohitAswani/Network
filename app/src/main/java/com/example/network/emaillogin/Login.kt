package com.example.network.emaillogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.network.MainActivity
import com.example.network.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var mAuth:FirebaseAuth

    private lateinit var binding:ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth= FirebaseAuth.getInstance()

        binding.signupButton.setOnClickListener{
            val intent= Intent(this, Signup::class.java)
            startActivity(intent)
        }

        binding.logInButton.setOnClickListener{
            val email="test3@gmail.com"
            val password="test1234"

            login(email,password)
        }
    }

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser!=null)
        {
            val intent=Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(email:String,password:String)
    {
        // logging in the user
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // code after successful login
                    val intent=Intent(this@Login, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@Login,"USER DOES NOT EXIST",Toast.LENGTH_SHORT).show()
                }
            }
    }
}