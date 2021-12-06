package com.example.network.status

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.network.R
import com.example.network.databinding.StatusFragmentBinding

class Status : Fragment() {

    companion object {
        fun newInstance() = Status()
    }

    private lateinit var viewModel: StatusViewModel

    private lateinit var binding:StatusFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.status_fragment,container,false)

        return binding.root
    }
}