package com.example.network.phoneAuth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.example.network.MainActivity
import com.example.network.R
import com.example.network.signup.SignUp
import com.example.network.utilities.Constants
import com.example.network.utilities.PreferenceManager
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_phonelogin.*
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit


class ChkOTP : AppCompatActivity() {
    private lateinit var input1: EditText
    private lateinit var input2: EditText
    private lateinit var input3: EditText
    private lateinit var input4: EditText
    private lateinit var input5: EditText
    private lateinit var input6: EditText
    private lateinit var verify: Button
    private lateinit var PB: ProgressBar
    private lateinit var Getotp: String
    private lateinit var auth: FirebaseAuth
    private lateinit var redo: TextView
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var phoneNumber: String

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chk_otp)

        preferenceManager = PreferenceManager(applicationContext)

        supportActionBar?.hide()

        input1 = findViewById(R.id.inputotp1)
        input3 = findViewById(R.id.inputotp3)
        input4 = findViewById(R.id.inputotp4)
        input5 = findViewById(R.id.inputotp5)
        input6 = findViewById(R.id.inputotp6)
        input2 = findViewById(R.id.inputotp2)
        verify = findViewById(R.id.OTP_button)
        PB = findViewById(R.id.Prog_send_otp)
        auth = FirebaseAuth.getInstance()
        phoneNumber = intent.getStringExtra("number")!!
        val mob: TextView = findViewById<TextView>(R.id.mobile).apply {
            text = phoneNumber.trim()
        }
        Getotp = intent.getStringExtra("storedVerificationId").toString()
        verify.setOnClickListener {
            if (input1.text.toString().trim().isNotEmpty() && input2.text.toString().trim()
                    .isNotEmpty() && input3.text.toString().trim()
                    .isNotEmpty() &&
                input4.text.toString().trim().isNotEmpty() && input5.text.toString().trim()
                    .isNotEmpty() && input6.text.toString().trim().isNotEmpty()
            ) {
                val sb = StringBuilder()
                sb.append(input1.text.toString())
                sb.append(input2.text.toString())
                sb.append(input3.text.toString())
                sb.append(input4.text.toString())
                sb.append(input5.text.toString())
                sb.append(input6.text.toString())
                val enteredotp: String = sb.toString()

                verify.visibility = View.GONE
                PB.visibility = View.VISIBLE
                Log.d("TAG", "the enterd otp $enteredotp")
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    Getotp, enteredotp
                )
                signInWithPhoneAuthCredential(credential)


                //Toast.makeText(this,"Verifying OTP", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter all 6 digit", Toast.LENGTH_SHORT).show()
            }
        }
        automove()
        redo = findViewById(R.id.recieveOTP)
        redo.setOnClickListener {
            sendVerificationcode("+91" + mob.text.toString().trim())
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
                Getotp = verificationId
                resendToken = token
            }
        }
    }

    private fun automove() {
        input1.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) //size as per your requirement
                {
                    input2.requestFocus()
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
        input2.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) //size as per your requirement
                {
                    input3.requestFocus()
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        input3.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                if (s.toString().trim().isNotEmpty()) //size as per your requirement
                {
                    input4.requestFocus()
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {

            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        input4.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) //size as per your requirement
                {
                    input5.requestFocus()
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {

            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        input5.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) //size as per your requirement
                {
                    input6.requestFocus()
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    signIn()
                } else {
                    // Sign in failed, display a message and update the UI

                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        verify.visibility = View.VISIBLE
                        PB.visibility = View.GONE
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
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

    private fun showToast(message: String) {
        Toast.makeText(this@ChkOTP, message, Toast.LENGTH_SHORT).show()
    }

    private fun signIn() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTIONS_USER)
            .whereEqualTo(Constants.KEY_PHONE, phoneNumber)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result != null && it.result!!.documents.size > 0) {
                    Log.i(TAG, "Present in database")
                    val documentSnapshot: DocumentSnapshot = it.result!!.documents[0]
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                    preferenceManager.putString(
                        Constants.KEY_NAME,
                        documentSnapshot.getString(Constants.KEY_NAME)!!
                    )
                    preferenceManager.putString(
                        Constants.KEY_IMAGE,
                        documentSnapshot.getString(Constants.KEY_IMAGE)!!
                    )
                    preferenceManager.putString(
                        Constants.KEY_STATUS,
                        documentSnapshot.getString(Constants.KEY_STATUS) ?:""
                    )
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    startActivity(intent)
                } else {
                    Log.i(TAG, "Not in database")
                    val intent = Intent(applicationContext, SignUp::class.java)
                    intent.putExtra("number", phoneNumber)
                    startActivity(intent)
                }
            }
    }

    private fun loading(isLoading: Boolean) {
        when (isLoading) {
            true -> {
                verify.visibility = View.GONE
                PB.visibility = View.VISIBLE
            }
            else -> {
                verify.visibility = View.VISIBLE
                PB.visibility = View.GONE
            }
        }
    }

    companion object {
        const val TAG = "OTP"
    }
}