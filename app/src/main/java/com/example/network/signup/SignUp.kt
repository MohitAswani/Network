package com.example.network.signup

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.network.MainActivity
import com.example.network.databinding.ActivitySignUpPhoneBinding
import com.example.network.utilities.Constants
import com.example.network.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class SignUp : AppCompatActivity() {


    private lateinit var binding: ActivitySignUpPhoneBinding

    private var encodedImage: String? = null

    private var phoneNumber: String? = null

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        phoneNumber = intent.getStringExtra("number")
        setListeners()
        supportActionBar?.hide()
    }

    private fun setListeners() {
        binding.signUpButton.setOnClickListener {
            if (isValidSignUp()) {
                signUp()
            }
        }
        binding.imageLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@SignUp, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        Log.d("SIGNED", preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN).toString())
        val user = HashMap<String, Any>()
        user[Constants.KEY_NAME] = binding.editName.text.toString().trim()
        user[Constants.KEY_PHONE] = phoneNumber.toString()
        user[Constants.KEY_IMAGE] = encodedImage.toString()
        user[Constants.KEY_STATUS] = binding.editStatus.text.toString()
        database.collection(Constants.KEY_COLLECTIONS_USER)
            .add(user)
            .addOnSuccessListener {
                loading(false)
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager.putString(Constants.KEY_USER_ID, it.id)
                preferenceManager.putString(
                    Constants.KEY_NAME,
                    binding.editName.text.toString().trim()
                )
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage!!)
                preferenceManager.putString(
                    Constants.KEY_STATUS,
                    binding.editStatus.text.toString()
                )
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK))
                startActivity(intent)
            }
            .addOnFailureListener {
                loading(false)
                showToast(it.message.toString())
            }

    }

    private fun encodeImage(bitmap: Bitmap): String {
        val pWidth = 150;
        val pHeight = bitmap.height * pWidth / bitmap.width
        val previewBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, pWidth, pHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri: Uri = result.data?.data!!
                try {
                    val inputStream = this.contentResolver.openInputStream(imageUri)
                    val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.userImage.setImageBitmap(bitmap)
                    binding.addImageText.visibility = View.GONE
                    encodedImage = encodeImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }

    private fun isValidSignUp(): Boolean {
        return when {
            encodedImage == null -> {
                showToast("Add the image")
                false
            }
            binding.editName.text.toString().trim().isEmpty() -> {
                showToast("Add the name")
                false
            }
            else -> {
                true
            }
        }
    }

    private fun loading(isLoading: Boolean) {
        when (isLoading) {
            true -> {
                binding.progSignUp.visibility = View.VISIBLE
                binding.signUpButton.visibility = View.GONE
            }
            else -> {
                binding.progSignUp.visibility = View.GONE
                binding.signUpButton.visibility = View.VISIBLE
            }
        }
    }

}