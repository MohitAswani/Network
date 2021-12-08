package com.example.network.recent

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.network.R
import com.example.network.adapters.RecentUserAdapter
import com.example.network.databinding.RecentFragmentBinding
import com.example.network.listeners.UserListener
import com.example.network.models.Users
import com.example.network.utilities.Constants
import com.example.network.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class Recent : Fragment(),UserListener{


    private lateinit var viewModel: RecentViewModel

    private lateinit var binding: RecentFragmentBinding

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.recent_fragment, container, false)

        preferenceManager = context?.let { PreferenceManager(it.applicationContext) }!!

        getUsers()

        return binding.root
    }

    private fun getUsers() {
        loading(true)

        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTIONS_USER+"/"+preferenceManager.getString(Constants.KEY_USER_ID)+"/"+Constants.KEY_SUB_COLLECTION_FRIENDS)
            .get()
            .addOnCompleteListener{
                if(it.isSuccessful&&it.result!=null)
                {
                    loading(false)
                    val friends=ArrayList<Users>()
                    for(document in it.result!!.documents)
                    {
                        val friend=Users(
                            name=document.getString(Constants.KEY_NAME)?:"name",
                            phoneNumber=null,
                            image =document.getString(Constants.KEY_IMAGE)?:"image",
                            token =document.getString(Constants.FCM_TOKEN)?:"token",
                            id = document.id
                        )
                        friends.add(friend)
                    }
                    if(friends.size>0)
                    {
                        val adapter=RecentUserAdapter(friends,this)
                        binding.userRecyclerView.adapter=adapter
                        binding.userRecyclerView.visibility=View.VISIBLE
                    }
                    else{
                        showErrorMessage()
                    }
                }
                else
                {
                    showErrorMessage()
                }
            }
    }

    private fun showErrorMessage() {
        binding.textErrorMessage.text = "No user available"
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean) {
        when (isLoading) {
            true -> binding.progressBar.visibility = View.VISIBLE
            else -> binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onUserClicked(user: Users) {

    }
}