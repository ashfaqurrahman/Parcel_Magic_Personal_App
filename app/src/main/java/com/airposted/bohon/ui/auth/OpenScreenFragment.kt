package com.airposted.bohon.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airposted.bohon.databinding.FragmentOpenScreenBinding
import com.airposted.bohon.utils.customTextView

class OpenScreenFragment : Fragment() {
    private lateinit var binding: FragmentOpenScreenBinding
    private var communicatorFragmentInterface: AuthCommunicatorFragmentInterface? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOpenScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        customTextView(binding.tvTermsConditionSignup, requireContext())
        communicatorFragmentInterface = context as AuthCommunicatorFragmentInterface
        binding.next.setOnClickListener {
            communicatorFragmentInterface!!.addContentFragment(PhoneNumberFragment(), true)
        }
    }
}