package com.example.network

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.network.adapters.ChatAdapter
import com.example.network.components.MyEditText
import com.example.network.components.MyEditText.KeyBoardInputCallbackListener
import com.example.network.databinding.ActivityChatBinding
import com.example.network.listeners.FileListener
import com.example.network.listeners.ImageListener
import com.example.network.models.ChatMessage
import com.example.network.models.Users
import com.example.network.networking.ApiClient
import com.example.network.networking.ApiService
import com.example.network.phoneAuth.ChkOTP.Companion.TAG
import com.example.network.utilities.Constants
import com.example.network.utilities.PreferenceManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.scottyab.aescrypt.AESCrypt
import com.vanniktech.emoji.EmojiPopup
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.item_container_sent_file.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class ChatActivity : BasicActivity(), ImageListener ,FileListener {

    private lateinit var binding: ActivityChatBinding

    private lateinit var rUser: Users

    private lateinit var chatMessages: ArrayList<ChatMessage>

    private lateinit var chatAdapter: ChatAdapter

    private lateinit var preferenceManager: PreferenceManager

    private lateinit var database: FirebaseFirestore

    private lateinit var storage: FirebaseStorage

    private lateinit var storageRef: StorageReference

    private var conversationId: String? = null

    private var isReceiverAvailable: Boolean = false

    private var lastSeen: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        clickListeners()
        init()
        setUserValues()
        listenMessages()
        keyEditText()
    }

    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(
            chatMessages,
            preferenceManager.getString(Constants.KEY_USER_ID),
            this,
            this
        )
        binding.chatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
        storage=Firebase.storage
        storageRef = storage.reference
    }

    private fun setUserValues() {
        rUser = intent.getSerializableExtra(Constants.KEY_USER) as Users
        binding.textName.text = rUser.name
        if (rUser.image != null) {
            binding.imageProfile.setImageBitmap(getUserImage(rUser.image!!))
        } else {
            database.collection(Constants.KEY_COLLECTIONS_USER).document(rUser.id)
                .get()
                .addOnSuccessListener {
                    if (it != null) {
                        binding.imageProfile.setImageBitmap(it.getString(Constants.KEY_IMAGE)
                            ?.let { it1 -> getUserImage(it1) })
                    }
                }
        }

    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
        super.onBackPressed()
    }

    private fun clickListeners() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        binding.sendButton.setOnClickListener {
            if (!binding.inputText.text.isNullOrBlank())
                sendMessage()
        }

        binding.attach.setOnClickListener {
//            pickImage()
            if (binding.attachAdder.visibility == View.VISIBLE)
                binding.attachAdder.visibility = View.GONE
            else
                binding.attachAdder.visibility = View.VISIBLE
        }

        binding.attachImage.setOnClickListener {
            pickImage()
        }

        binding.attachCamera.setOnClickListener {
            clickImage()
        }

        binding.attachFile.setOnClickListener{
            pickFile()
        }


        binding.camera.setOnClickListener {
            clickImage()
        }

        val popup = EmojiPopup.Builder.fromRootView(binding.root).build(binding.inputText)

        binding.emoji.setOnClickListener {
            if(!popup.isShowing) {
                binding.emoji.setImageResource(R.drawable.ic_round_keyboard_24)
            }
            else
                binding.emoji.setImageResource(R.drawable.ic_round_emoji_emotions)

            popup.toggle()
        }
    }

    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun sendMessage() {

        if(preferenceManager.getString(rUser.id)=="null")
            preferenceManager.putString(rUser.id,getRandomString(20))

        Log.d("ChatActivity",preferenceManager.getString(rUser.id))

        val message = HashMap<String, Any>()
        Log.i("YOLO", "id not found")
        message[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID)
        message[Constants.KEY_RECEIVER_ID] = rUser.id
        message[Constants.KEY_MESSAGE] = AESCrypt.encrypt(preferenceManager.getString(rUser.id),binding.inputText.text.toString().trim())
        message[Constants.KEY_TIMESTAMP] = Date()
        message[Constants.KEY_FILE_TYPE] = Constants.KEY_TEXT_TYPE
        message[Constants.KEY_FILE]= "not present"
        Log.d("YOLO", "id found")

        database.collection(
            Constants.KEY_COLLECTION_CHATS
        ).add(message)

        if (conversationId != null) {
            updateConversation(AESCrypt.encrypt(preferenceManager.getString(rUser.id),binding.inputText.text.toString().trim()))
        } else {
            val conversation = HashMap<String, Any?>()
            conversation[Constants.KEY_SENDER_ID] =
                preferenceManager.getString(Constants.KEY_USER_ID)
            conversation[Constants.KEY_SENDER_NAME] =
                preferenceManager.getString(Constants.KEY_NAME)
            conversation[Constants.KEY_SENDER_IMAGE] =
                preferenceManager.getString(Constants.KEY_IMAGE)
            conversation[Constants.KEY_RECEIVER_ID] = rUser.id
            conversation[Constants.KEY_RECEIVER_NAME] = rUser.name
            conversation[Constants.KEY_RECEIVER_IMAGE] = rUser.image
            conversation[Constants.KEY_RECENT_MESSAGE] = AESCrypt.encrypt(preferenceManager.getString(rUser.id),binding.inputText.text.toString().trim())
            conversation[Constants.KEY_TIMESTAMP] = Date()
            conversation[Constants.KEY_ENCRYPT_KEY]=preferenceManager.getString(rUser.id)
            addConversation(conversation)
        }
        if (!isReceiverAvailable) {
            try {
                val tokens = JSONArray()
                tokens.put(rUser.token)

                val data = JSONObject()
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME))
                data.put(Constants.FCM_TOKEN, preferenceManager.getString(Constants.FCM_TOKEN))
                data.put(Constants.KEY_MESSAGE, AESCrypt.encrypt(preferenceManager.getString(rUser.id),binding.inputText.text.toString().trim()))

                val body = JSONObject()
                body.put(Constants.REMOTE_MESSAGE_DATA, data)
                body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS, tokens)

                sendNotification(body.toString())
            } catch (exception: Exception) {
                exception.message?.let { showToast(it) }
            }
        }
        binding.inputText.text = null
    }

    @SuppressLint("SetTextI18n")
    private fun listenAvailabilityOfReceiver() {

        database.collection(Constants.KEY_COLLECTIONS_USER)
            .document(rUser.id)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen:error", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val status: Boolean? = snapshot.getBoolean(Constants.KEY_IS_ONLINE)
                    val lastTimestamp: Date? = snapshot.getDate(Constants.LAST_SEEN)
                    if (status != null) {
                        isReceiverAvailable = status
                    }

                    if (lastTimestamp != null) {
                        lastSeen = lastTimestamp
                    }
                    rUser.token = snapshot.getString(Constants.FCM_TOKEN)
                }
                if (isReceiverAvailable) {
                    binding.availability.text = "Online"
                } else {
                    if (lastSeen != null)
                        binding.availability.text =
                            "last seen" + lastSeen?.let { getReadableDate(it) }
                }
            }
    }

    private fun listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHATS)
            .whereEqualTo(
                Constants.KEY_SENDER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .whereEqualTo(Constants.KEY_RECEIVER_ID, rUser.id)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen:error", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val count = chatMessages.size
                    for (dc in snapshots.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val message = ChatMessage(
                                    senderId = dc.document.getString(Constants.KEY_SENDER_ID)!!,
                                    receiverId = dc.document.getString(Constants.KEY_RECEIVER_ID)!!,
                                    message = AESCrypt.decrypt(preferenceManager.getString(rUser.id),dc.document.getString(Constants.KEY_MESSAGE)),
                                    dateTime = getReadableDateChat(dc.document.getDate(Constants.KEY_TIMESTAMP)!!),
                                    dateObject = dc.document.getDate(Constants.KEY_TIMESTAMP)!!,
                                    fileType = dc.document.getString(Constants.KEY_FILE_TYPE) ?: "text",
                                    file = dc.document.getString(Constants.KEY_FILE)?:"null"
                                )
                                message.message=message.message?.trim()
                                chatMessages.add(message)
                            }
                            else -> {}
                        }
                    }
                    chatMessages.sortBy { it.dateObject }
                    if (count == 0) {
                        chatAdapter.notifyDataSetChanged()
                    } else {
                        chatAdapter.notifyItemRangeInserted(count, chatMessages.size - count)
                        binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
                    }
                    binding.chatRecyclerView.visibility = View.VISIBLE
                }
                binding.progressBar.visibility = View.GONE
                if (conversationId == null) {
                    checkForConversation()
                }
            }

        database.collection(Constants.KEY_COLLECTION_CHATS)
            .whereEqualTo(Constants.KEY_SENDER_ID, rUser.id)
            .whereEqualTo(
                Constants.KEY_RECEIVER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen:error", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val count = chatMessages.size
                    for (dc in snapshots.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val message = ChatMessage(
                                    senderId = dc.document.getString(Constants.KEY_SENDER_ID)!!,
                                    receiverId = dc.document.getString(Constants.KEY_RECEIVER_ID)!!,
                                    message = AESCrypt.decrypt(preferenceManager.getString(rUser.id),dc.document.getString(Constants.KEY_MESSAGE)),
                                    dateTime = getReadableDateChat(dc.document.getDate(Constants.KEY_TIMESTAMP)!!),
                                    dateObject = dc.document.getDate(Constants.KEY_TIMESTAMP)!!,
                                    fileType = dc.document.getString(Constants.KEY_FILE_TYPE) ?: "text",
                                    file = dc.document.getString(Constants.KEY_FILE)?:"null"
                                )
                                message.message=message.message?.trim()
                                chatMessages.add(message)
                            }
                            else -> {}
                        }
                    }
                    chatMessages.sortBy { it.dateObject }
                    if (count == 0) {
                        chatAdapter.notifyDataSetChanged()
                    } else {
                        chatAdapter.notifyItemRangeInserted(count, chatMessages.size - count)
                        binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
                    }
                    binding.chatRecyclerView.visibility = View.VISIBLE
                }
                binding.progressBar.visibility = View.GONE
                if (conversationId == null) {
                    checkForConversation()
                }
            }
    }

    private fun addConversation(conversation: HashMap<String, Any?>) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
            .add(conversation)
            .addOnSuccessListener {
                conversationId = it.id
            }

        val data=HashMap<String,Any>()
        data["user1"]=preferenceManager.getString(Constants.KEY_USER_ID)
        data["user2"]=rUser.id
        data["key"]=conversation[Constants.KEY_ENCRYPT_KEY].toString()

        database.collection("keys")
            .add(data)

    }

    private fun updateConversation(message: String) {
        conversationId?.let {
            database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .document(it)
        }?.update(
            Constants.KEY_RECENT_MESSAGE, message,
            Constants.KEY_TIMESTAMP, Date()
        )
    }

    private fun checkForConversation() {
        if (chatMessages.size > 0) {
            checkForConversationRemotely(
                preferenceManager.getString(Constants.KEY_USER_ID),
                rUser.id
            )
            checkForConversationRemotely(
                rUser.id,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
        }
    }

    private fun checkForConversationRemotely(senderId: String, receiverId: String) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result != null && it.result!!.documents.size > 0) {
                    conversationId = it.result!!.documents[0].id
                }
            }

    }

    private fun sendNotification(messageBody: String) {
        ApiClient.getClient().create(ApiService::class.java).sendMessage(
            headers = Constants.getMyRemoteMessageHeaders(),
            message = messageBody
        ).enqueue(object : retrofit2.Callback<String> {
            override fun onResponse(
                @NonNull call: Call<String>,
                @NonNull response: Response<String>
            ) {
                if (response.isSuccessful) {
                    try {
                        val responseJson: JSONObject = JSONObject(response.body())
                        val results: JSONArray = responseJson.getJSONArray("results")
                        if (responseJson.getInt("failure") == 1) {
                            val error = results.get(0) as JSONObject
                            showToast(error.getString("error"))
                            return
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    showToast("Error: " + response.code())
                }
            }

            override fun onFailure(@NonNull call: Call<String>, @NonNull t: Throwable) {
                t.message?.let { showToast(it) }
            }
        })

    }

    private fun getUserImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun getReadableDate(date: Date): String {
        return if (date.date == Date().date)
            " today at " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        else if ((date.date - Date().date) == 1)
            " yesterday at " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        else
            " on " + SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
    }

    private fun getReadableDateChat(date: Date): String {
        return if (date.date == Date().date)
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        else
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
    }

    private fun pickImage() {
        ImagePicker.with(this)
            .galleryOnly()
            .crop()                    //Crop image(Optional), Check Customization for more option
            .compress(512)            //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            )    //Final image resolution will be less than 1080 x 1080(Optional)
            .createIntent { intent ->
                startForImageResult.launch(intent)
            }
    }

    private fun clickImage() {
        ImagePicker.with(this)
            .cameraOnly()
            .crop()                    //Crop image(Optional), Check Customization for more option
            .compress(512)            //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            )    //Final image resolution will be less than 1080 x 1080(Optional)
            .createIntent { intent ->
                startForImageResult.launch(intent)
            }
    }

    private val startForImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri: Uri = result.data?.data!!
                try {
                    val inputStream = contentResolver.openInputStream(imageUri)
                    val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                    sendImages(encodeImage(bitmap))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }

    private fun encodeImage(bitmap: Bitmap): String {
        val pWidth = bitmap.width
        val pHeight = bitmap.height
        val previewBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, pWidth, pHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun sendImages(image: String) {
        val message = HashMap<String, Any>()
        Log.i("YOLO", "id not found")
        message[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID)
        message[Constants.KEY_RECEIVER_ID] = rUser.id
        message[Constants.KEY_MESSAGE] = AESCrypt.encrypt(preferenceManager.getString(rUser.id),binding.inputText.text.toString().trim())
        message[Constants.KEY_TIMESTAMP] = Date()
        message[Constants.KEY_FILE_TYPE] = Constants.KEY_IMAGE_TYPE
        message[Constants.KEY_FILE]=image
        Log.d("YOLO", "id found")

        database.collection(
            Constants.KEY_COLLECTION_CHATS
        ).add(message)

        if (conversationId != null) {
            updateConversation(AESCrypt.encrypt(preferenceManager.getString(rUser.id)," \uD83D\uDCF7 IMAGE"))
        } else {
            val conversation = HashMap<String, Any?>()
            conversation[Constants.KEY_SENDER_ID] =
                preferenceManager.getString(Constants.KEY_USER_ID)
            conversation[Constants.KEY_SENDER_NAME] =
                preferenceManager.getString(Constants.KEY_NAME)
            conversation[Constants.KEY_SENDER_IMAGE] =
                preferenceManager.getString(Constants.KEY_IMAGE)
            conversation[Constants.KEY_RECEIVER_ID] = rUser.id
            conversation[Constants.KEY_RECEIVER_NAME] = rUser.name
            conversation[Constants.KEY_RECEIVER_IMAGE] = rUser.image
            conversation[Constants.KEY_RECENT_MESSAGE] = AESCrypt.encrypt(preferenceManager.getString(rUser.id)," \uD83D\uDCF7 IMAGE")
            conversation[Constants.KEY_TIMESTAMP] = Date()
            addConversation(conversation)
        }
        if (!isReceiverAvailable) {
            try {
                val tokens = JSONArray()
                tokens.put(rUser.token)

                val data = JSONObject()
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME))
                data.put(Constants.FCM_TOKEN, preferenceManager.getString(Constants.FCM_TOKEN))
                data.put(Constants.KEY_MESSAGE, " \uD83D\uDCF7 IMAGE")

                val body = JSONObject()
                body.put(Constants.REMOTE_MESSAGE_DATA, data)
                body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS, tokens)

                sendNotification(body.toString())
            } catch (exception: Exception) {
                exception.message?.let { showToast(it) }
            }
        }
    }

    override fun onImageClicked(image: String) {
        val intent = Intent(applicationContext, ImageViewer::class.java)
        intent.putExtra("image", image)
        startActivity(intent)
    }

    private fun pickFile(){
        val intent=Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/*"
        startForFileResult.launch(intent)
    }


    private val startForFileResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val fileUri: Uri = result.data?.data!!
                try {
                    uploadingFiles(fileUri)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }

    private fun uploadingFiles(uri:Uri)
    {
        val dialogBox=ProgressDialog(this)
        dialogBox.setMessage("Uploading")

        dialogBox.show()

        val ref = storageRef.child("files/${"test.pdf"}")
        val uploadTask = ref.putFile(uri)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                dialogBox.dismiss()
                showToast("File uploaded successfullly")
                if(binding.inputText.text.isNullOrEmpty())
                    sendFiles(task.result.toString(),"Unknown"+".pdf")
                else
                    sendFiles(task.result.toString(),binding.inputText.text.toString().trim()+".pdf")
            } else {
                showToast("File cannot be uploaded : ${task.result}")
            }
        }

    }


    private fun sendFiles(fileReference: String,fileName:String) {
        val message = HashMap<String, Any>()
        Log.i("YOLO", "id not found")
        message[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID)
        message[Constants.KEY_RECEIVER_ID] = rUser.id
        message[Constants.KEY_MESSAGE] = AESCrypt.encrypt(preferenceManager.getString(rUser.id),fileName)
        message[Constants.KEY_TIMESTAMP] = Date()
        message[Constants.KEY_FILE_TYPE] = Constants.KEY_FILE_TYPE
        message[Constants.KEY_FILE]=fileReference
        Log.d("YOLO", "id found")

        database.collection(
            Constants.KEY_COLLECTION_CHATS
        ).add(message)

        if (conversationId != null) {
            updateConversation(AESCrypt.encrypt(preferenceManager.getString(rUser.id),"📄 Document"))
        } else {
            val conversation = HashMap<String, Any?>()
            conversation[Constants.KEY_SENDER_ID] =
                preferenceManager.getString(Constants.KEY_USER_ID)
            conversation[Constants.KEY_SENDER_NAME] =
                preferenceManager.getString(Constants.KEY_NAME)
            conversation[Constants.KEY_SENDER_IMAGE] =
                preferenceManager.getString(Constants.KEY_IMAGE)
            conversation[Constants.KEY_RECEIVER_ID] = rUser.id
            conversation[Constants.KEY_RECEIVER_NAME] = rUser.name
            conversation[Constants.KEY_RECEIVER_IMAGE] = rUser.image
            conversation[Constants.KEY_RECENT_MESSAGE] = AESCrypt.encrypt(preferenceManager.getString(rUser.id),"📄 Document")
            conversation[Constants.KEY_TIMESTAMP] = Date()
            addConversation(conversation)
        }
        if (!isReceiverAvailable) {
            try {
                val tokens = JSONArray()
                tokens.put(rUser.token)

                val data = JSONObject()
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME))
                data.put(Constants.FCM_TOKEN, preferenceManager.getString(Constants.FCM_TOKEN))
                data.put(Constants.KEY_MESSAGE, "📄 Document")

                val body = JSONObject()
                body.put(Constants.REMOTE_MESSAGE_DATA, data)
                body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS, tokens)

                sendNotification(body.toString())
            } catch (exception: Exception) {
                exception.message?.let { showToast(it) }
            }
        }
        binding.inputText.text = null
    }

    override fun onFileClicked(file: String,fileName: String) {
        Log.d("ChatActivity","file being download ${file}")
        val request=DownloadManager.Request(Uri.parse(file))
        val title=URLUtil.guessFileName(file,null,null)
        request.setTitle(title)
        request.setDescription("Downloading")
        val cookie=CookieManager.getInstance().getCookie(file)
        request.addRequestHeader("cookie",cookie)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${System.currentTimeMillis()}.pdf")

        val downloadManager=getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    private fun keyEditText()
    {
        val myEditText = findViewById<MyEditText>(R.id.inputText)
        myEditText.setKeyBoardInputCallbackListener(object : KeyBoardInputCallbackListener {
            override fun onCommitContent(
                inputContentInfo: InputContentInfoCompat?,
                flags: Int, opts: Bundle?
            ) {
                sendGifsAndStickers(inputContentInfo?.linkUri.toString())
                if (inputContentInfo != null) {
                    Log.d("ChatActivity",inputContentInfo.description.getMimeType(0))
                }
            }
        })
    }

    private fun sendGifsAndStickers(fileReference: String) {
        val message = HashMap<String, Any>()
        Log.i("YOLO", "id not found")
        message[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID)
        message[Constants.KEY_RECEIVER_ID] = rUser.id
        message[Constants.KEY_MESSAGE] = AESCrypt.encrypt(preferenceManager.getString(rUser.id),"null")
        message[Constants.KEY_TIMESTAMP] = Date()
        message[Constants.KEY_FILE_TYPE] = Constants.KEY_GIFS_STICKERS_TYPE
        message[Constants.KEY_FILE]=fileReference
        Log.d("YOLO", "id found")

        database.collection(
            Constants.KEY_COLLECTION_CHATS
        ).add(message)

        if (conversationId != null) {
            updateConversation(AESCrypt.encrypt(preferenceManager.getString(rUser.id),"\uD83D\uDC7E Sticker"))
        } else {
            val conversation = HashMap<String, Any?>()
            conversation[Constants.KEY_SENDER_ID] =
                preferenceManager.getString(Constants.KEY_USER_ID)
            conversation[Constants.KEY_SENDER_NAME] =
                preferenceManager.getString(Constants.KEY_NAME)
            conversation[Constants.KEY_SENDER_IMAGE] =
                preferenceManager.getString(Constants.KEY_IMAGE)
            conversation[Constants.KEY_RECEIVER_ID] = rUser.id
            conversation[Constants.KEY_RECEIVER_NAME] = rUser.name
            conversation[Constants.KEY_RECEIVER_IMAGE] = rUser.image
            conversation[Constants.KEY_RECENT_MESSAGE] = AESCrypt.encrypt(preferenceManager.getString(rUser.id),"\uD83D\uDC7E Sticker")
            conversation[Constants.KEY_TIMESTAMP] = Date()
            addConversation(conversation)
        }
        if (!isReceiverAvailable) {
            try {
                val tokens = JSONArray()
                tokens.put(rUser.token)

                val data = JSONObject()
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME))
                data.put(Constants.FCM_TOKEN, preferenceManager.getString(Constants.FCM_TOKEN))
                data.put(Constants.KEY_MESSAGE, "\uD83D\uDC7E Sticker")

                val body = JSONObject()
                body.put(Constants.REMOTE_MESSAGE_DATA, data)
                body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS, tokens)

                sendNotification(body.toString())
            } catch (exception: Exception) {
                exception.message?.let { showToast(it) }
            }
        }
        binding.inputText.text = null
    }



}