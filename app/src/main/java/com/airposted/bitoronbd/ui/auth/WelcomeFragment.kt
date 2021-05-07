package com.airposted.bitoronbd.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airposted.bitoronbd.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding
    private var communicatorFragmentInterface: AuthCommunicatorFragmentInterface? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        communicatorFragmentInterface = context as AuthCommunicatorFragmentInterface
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.next.setOnClickListener {
            val fragment = OTPFragment()
            val bundle = Bundle()
            bundle.putString("phone", requireArguments().getString("phone"))
            bundle.putString("token", requireArguments().getString("token"))
            bundle.putInt("id", requireArguments().getInt("id"))
            bundle.putString("image", requireArguments().getString("image"))
            bundle.putString("name", requireArguments().getString("name"))
            bundle.putBoolean("isAuth", true)
            fragment.arguments = bundle
            communicatorFragmentInterface?.addContentFragment(fragment, true)
        }

    }
}