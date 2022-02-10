package com.example.network.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.network.ChatActivity
import com.example.network.R
import com.example.network.models.Users
import com.example.network.utilities.Constants
import com.example.network.utilities.PreferenceManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.scottyab.aescrypt.AESCrypt
import java.util.*

class MessagingService : FirebaseMessagingService() {

    private lateinit var preferenceManager: PreferenceManager


    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val user=Users(
            id =remoteMessage.data[Constants.KEY_USER_ID].toString(),
            name = remoteMessage.data[Constants.KEY_NAME].toString(),
            token = remoteMessage.data[Constants.FCM_TOKEN].toString(),
            image = null,
            phoneNumber = null,
            status = null
        )

        val notificationId=Random().nextInt()
        val channelId="chat_message"

        val intent=Intent(this,ChatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK))
        intent.putExtra(Constants.KEY_USER,user)
        val pendingIntent=PendingIntent.getActivity(this,0,intent,0)

        val builder=NotificationCompat.Builder(this,channelId)
        builder.setSmallIcon(R.drawable.ic_notifications)
        builder.setContentTitle(user.name)
        builder.setContentText(remoteMessage.data[Constants.KEY_MESSAGE])
        builder.setStyle(NotificationCompat.BigTextStyle()
            .bigText(remoteMessage.data[Constants.KEY_MESSAGE]))
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channelName="Chat Message"
            val channelDescription="This notification channel is used for chat message notification"
            val importance=NotificationManager.IMPORTANCE_DEFAULT
            val channel=NotificationChannel(channelId,channelName,importance)
            channel.description=channelDescription
            val notificationManager=getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        val notificationManagerCompat=NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId,builder.build())
     }
}