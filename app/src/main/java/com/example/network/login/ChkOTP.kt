package com.example.network.login

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable

import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.example.network.MainActivity
import com.example.network.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_phonelogin.*
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit


class ChkOTP : AppCompatActivity() {
    lateinit var  input1:EditText
    lateinit var  input2:EditText
    lateinit var  input3:EditText
    lateinit var  input4:EditText
    lateinit var  input5:EditText
    lateinit var  input6:EditText
    lateinit var  verify:Button
    lateinit var  PB:ProgressBar
    lateinit var Getotp:String
    lateinit var auth: FirebaseAuth
    lateinit var redo:TextView
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chk_otp)

        supportActionBar?.hide()

        input1=findViewById(R.id.inputotp1)
        input3=findViewById(R.id.inputotp3)
        input4=findViewById(R.id.inputotp4)
        input5=findViewById(R.id.inputotp5)
        input6=findViewById(R.id.inputotp6)
        input2=findViewById(R.id.inputotp2)
        verify=findViewById(R.id.OTP_button)
        PB=findViewById(R.id.Prog_send_otp)
        auth=FirebaseAuth.getInstance()
        val mob:TextView =findViewById<TextView>(R.id.mobile).apply {
            text=intent.getStringExtra("number")
        }
        Getotp= intent.getStringExtra("storedVerificationId").toString()
       verify.setOnClickListener(){
           if(!input1.text.toString().trim().isEmpty() && !input2.text.toString().trim().isEmpty() && !input3.text.toString().trim().isEmpty() &&
               !input4.text.toString().trim().isEmpty() && !input5.text.toString().trim().isEmpty()&& !input6.text.toString().trim().isEmpty()){
               val sb=StringBuilder()
               sb.append(input1.text.toString())
               sb.append(input2.text.toString())
               sb.append(input3.text.toString())
               sb.append(input4.text.toString())
               sb.append(input5.text.toString())
               sb.append(input6.text.toString())
               val enteredotp:String=sb.toString()

                  verify.visibility= View.GONE
               PB.visibility=View.VISIBLE
                  Log.d("TAG","the enterd otp $enteredotp")
                  val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                      Getotp, enteredotp)
                  signInWithPhoneAuthCredential(credential)


           //Toast.makeText(this,"Verifying OTP", Toast.LENGTH_SHORT).show()
           }else{
               Toast.makeText(this,"Please enter all 6 digit",Toast.LENGTH_SHORT).show()
           }
       }
        automove()
       redo=findViewById(R.id.recieveOTP)
        redo.setOnClickListener {
            sendVerificationcode("+91"+mob.text.toString().trim())
        }
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            }

            override fun onVerificationFailed(e: FirebaseException) {

                Toast.makeText(
                    applicationContext,
                    "Failed $edit_number.text.toString().trim()",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {


                Log.d("TAG", "onCodeSent:$verificationId")
                Getotp=verificationId
                resendToken = token
            }
        }
    }
   private fun automove(){
        input1.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // TODO Auto-generated method stub
                if (!s.toString().trim().isEmpty()) //size as per your requirement
                {
                    input2.requestFocus()
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
                // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub
            }
        })
        input2.addTextChangedListener(object : TextWatcher {
           override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
               // TODO Auto-generated method stub
               if (!s.toString().trim().isEmpty()) //size as per your requirement
               {
                   input3.requestFocus()
               }
           }

           override fun beforeTextChanged(
               s: CharSequence, start: Int,
               count: Int, after: Int
           ) {
               // TODO Auto-generated method stub
           }

           override fun afterTextChanged(s: Editable) {
               // TODO Auto-generated method stub
           }
       })
        input3.addTextChangedListener(object : TextWatcher {
           override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
               // TODO Auto-generated method stub
               if (!s.toString().trim().isEmpty()) //size as per your requirement
               {
                   input4.requestFocus()
               }
           }

           override fun beforeTextChanged(
               s: CharSequence, start: Int,
               count: Int, after: Int
           ) {
               // TODO Auto-generated method stub
           }

           override fun afterTextChanged(s: Editable) {
               // TODO Auto-generated method stub
           }
       })
        input4.addTextChangedListener(object : TextWatcher {
           override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
               // TODO Auto-generated method stub
               if (!s.toString().trim().isEmpty()) //size as per your requirement
               {
                   input5.requestFocus()
               }
           }

           override fun beforeTextChanged(
               s: CharSequence, start: Int,
               count: Int, after: Int
           ) {
               // TODO Auto-generated method stub
           }

           override fun afterTextChanged(s: Editable) {
               // TODO Auto-generated method stub
           }
       })
        input5.addTextChangedListener(object : TextWatcher {
           override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
               // TODO Auto-generated method stub
               if (!s.toString().trim().isEmpty()) //size as per your requirement
               {
                   input6.requestFocus()
               }
           }

           override fun beforeTextChanged(
               s: CharSequence, start: Int,
               count: Int, after: Int
           ) {
               // TODO Auto-generated method stub
           }

           override fun afterTextChanged(s: Editable) {
               // TODO Auto-generated method stub
           }
       })
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    var intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Sign in failed, display a message and update the UI

                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        verify.visibility= View.VISIBLE
                        PB.visibility=View.GONE
                        Toast.makeText(this,"Invalid OTP",Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
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