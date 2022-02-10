package com.example.network.utilities

class Constants {
    companion object{
        const val KEY_COLLECTIONS_USER="users"
        const val KEY_NAME="name"
        const val KEY_PHONE="phone"
        const val KEY_PREFERENCE_NAME="networkPreference"
        const val KEY_IS_SIGNED_IN="isSignedIn"
        const val KEY_USER_ID="userID"
        const val KEY_IMAGE="image"
        const val KEY_STATUS="status"
        const val FCM_TOKEN="fcmtoken"
        const val KEY_USER="user"

        const val KEY_COLLECTION_CHATS="chats"

        const val KEY_SENDER_ID="senderId"
        const val KEY_RECEIVER_ID="receiverId"
        const val KEY_MESSAGE = "message"
        const val KEY_TIMESTAMP= "timestamp"
        const val KEY_FILE="file"
        const val KEY_FILE_TYPE="fileType"
        const val KEY_IMAGE_TYPE="image"
        const val KEY_TEXT_TYPE="text"
        const val KEY_GIFS_STICKERS_TYPE="GifsAndStickers"

        const val KEY_COLLECTION_CONVERSATION="conversation"
        const val KEY_SENDER_NAME="senderName"
        const val KEY_SENDER_IMAGE="senderImage"
        const val KEY_RECEIVER_NAME="receiverName"
        const val KEY_RECEIVER_IMAGE="receiverImage"
        const val KEY_RECENT_MESSAGE="recentMessage"

        const val KEY_IS_ONLINE="online"
        const val LAST_SEEN="lastseen"

        const val REMOTE_MESSAGE_AUTHORIZATION="Authorization"
        const val REMOTE_MESSAGE_CONTENT_TYPE="Content-Type"
        const val REMOTE_MESSAGE_DATA="data"
        const val REMOTE_MESSAGE_REGISTRATION_IDS="registration_ids"

        private var remoteMessageHeaders:HashMap<String,String>?=null

        public fun getMyRemoteMessageHeaders():HashMap<String,String>{
            if(remoteMessageHeaders==null)
            {
                remoteMessageHeaders=HashMap<String,String>()
                remoteMessageHeaders!![REMOTE_MESSAGE_AUTHORIZATION] = "key=AAAANoiywbQ:APA91bGBrNz-vELH54v-CkN5NmcgorhgxQlXC_qiHZnV_JXHRuQBkc-ywA_F_ueA2tGLPjScHhXfcQqGDSuIBVIEVoeBU87Z9Y_N5Pnd_dcXL8qelBd8CI84VTAhHDPPheDh2tC_doDU"
                remoteMessageHeaders!![REMOTE_MESSAGE_CONTENT_TYPE] = "application/json"
            }
            return remoteMessageHeaders as HashMap<String, String>
        }

        const val KEY_ENCRYPT_KEY="key"

        private var encryptionKeys=HashMap<String,String>()

        public fun getKey(user:String):String?{
            return if(encryptionKeys[user] ==null) {
                null
            } else {
                encryptionKeys[user]
            }
        }
        public fun addKey(user:String,key:String){
            encryptionKeys[user] = key
        }

    }
}