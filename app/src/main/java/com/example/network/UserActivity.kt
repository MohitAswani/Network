package com.example.network

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.network.adapters.UsersAdapter
import com.example.network.databinding.ActivityUserBinding
import com.example.network.listeners.UserListener
import com.example.network.models.Users
import com.example.network.utilities.Constants
import com.example.network.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class UserActivity : BasicActivity(),UserListener{

    private lateinit var binding:ActivityUserBinding

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager= PreferenceManager(applicationContext)
        getUsers()
        setListeners()
    }

    private fun setListeners(){
        binding.backButton.setOnClickListener{
            onBackPressed()
        }
    }
    private fun getUsers(){
        loading(true)

        val database=FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTIONS_USER)
            .get()
            .addOnCompleteListener{
                loading(false)
                val currentUserId=preferenceManager.getString(Constants.KEY_USER_ID)
                if(it.isSuccessful&&it.result!=null){
                    val users=ArrayList<Users>()
                    for(document in it.result!!)
                    {
                        if(currentUserId == document.id)
                            continue

                        val phoneno: String =document.getString(Constants.KEY_PHONE)?:"xxxxxxxxxx"
                        val user=Users(
                            name =document.getString(Constants.KEY_NAME)?:"name",
                            phoneNumber ="+91"+" "+phoneno.substring(0,5)+" "+phoneno.substring(5),
                            image =document.getString(Constants.KEY_IMAGE)?:"image",
                            token =document.getString(Constants.FCM_TOKEN)?:"token",
                            id = document.id,
                            status =document.getString(Constants.KEY_STATUS)?:""
                        )
                        users.add(user)
                    }
                    if(users.size>0){
                        val usersAdapter=UsersAdapter(users,this)
                        binding.userRecyclerView.adapter=usersAdapter
                        binding.userRecyclerView.visibility=View.VISIBLE
                    }
                    else
                    {
                        showErrorMessage()
                    }
                }
                else
                {
                    showErrorMessage()
                }
            }
    }

    private fun showErrorMessage(){
        binding.textErrorMessage.text = R.string.no_user_error.toString()
        binding.textErrorMessage.visibility=View.VISIBLE

    }
    private fun loading(isLoading:Boolean)
    {
        when(isLoading){
            true-> binding.progressBar.visibility=View.VISIBLE
            else-> binding.progressBar.visibility=View.INVISIBLE
        }
    }

    override fun onUserClicked(user: Users) {
        val intent= Intent(applicationContext,ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER,user)
        startActivity(intent)
        finish()
    }
}