package com.example.network.emaillogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.network.MainActivity
import com.example.network.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class Signup : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var binding:ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth= FirebaseAuth.getInstance()

        binding.signupButton.setOnClickListener{
            val name=binding.editName.text.toString()
            val email=binding.editEmail.text.toString()
            val password=binding.editPassword.text.toString()

            signUp(name,email,password)
        }

    }

    private fun signUp(name:String,email:String,password:String)
    {
        // creating the user
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent=Intent(this@Signup, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@Signup,"ERROR :(",Toast.LENGTH_SHORT).show()

                }
            }
    }
}