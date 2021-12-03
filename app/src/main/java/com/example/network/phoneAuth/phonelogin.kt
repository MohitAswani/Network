package com.example.network.phoneAuth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.network.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_phonelogin.*
import java.util.concurrent.TimeUnit

class phonelogin : AppCompatActivity() {
    lateinit var editnumber :EditText
    lateinit var GetOTP :Button
    lateinit var PG :ProgressBar
    lateinit var auth: FirebaseAuth
    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
   lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        auth= FirebaseAuth.getInstance()
        setContentView(R.layout.activity_phonelogin)
       editnumber=findViewById(R.id.edit_number)
        GetOTP=findViewById(R.id.OTP_button)
        PG=findViewById(R.id.Prog_send_otp)

      GetOTP.setOnClickListener(){
        if(!edit_number.text.toString().trim().isEmpty()){
            if(edit_number.text.toString().trim().length==10){
               PG.visibility= View.VISIBLE
                GetOTP.visibility=View.INVISIBLE

                sendVerificationcode("+91"+editnumber.text.toString().trim())

            }else{
                Toast.makeText(this,"Please enter correct number",Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this,"Enter Mobile Number",Toast.LENGTH_SHORT).show()
        }
      }
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                PG.visibility= View.GONE
                GetOTP.visibility=View.VISIBLE
            }

            override fun onVerificationFailed(e: FirebaseException) {
                PG.visibility= View.GONE
                GetOTP.visibility=View.VISIBLE
                Toast.makeText(applicationContext, "Failed $edit_number.text.toString().trim()", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {

                PG.visibility= View.GONE
                GetOTP.visibility=View.VISIBLE
                Log.d("TAG","onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                var intent = Intent(applicationContext, ChkOTP::class.java)
                intent.putExtra("storedVerificationId",storedVerificationId)
                intent.putExtra("number",edit_number.text.toString())
                startActivity(intent)
            }


        }
    }
    private fun sendVerificationcode(number: String) {

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

}