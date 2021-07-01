package com.airposted.bohon.ui.auth

import android.os.Bundle
import android.util.MalformedJsonException
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.airposted.bohon.databinding.FragmentWelcomeBinding
import com.airposted.bohon.utils.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class WelcomeFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel
    private lateinit var binding: FragmentWelcomeBinding
    private var communicatorFragmentInterface: AuthCommunicatorFragmentInterface? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
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
            sendOTP()
        }

    }

    private fun sendOTP() {
        setProgressDialog(requireActivity())
        lifecycleScope.launch {
            try {
                val response = viewModel.sendOTP(
                    requireArguments().getString("phone")!!
                )
                if (response.success) {
                    val fragment = OTPFragment()
                    val bundle = Bundle()
                    bundle.putString("otp", response.data?.token)
                    bundle.putString("phone", requireArguments().getString("phone"))
                    bundle.putString("token", requireArguments().getString("token"))
                    bundle.putInt("id", requireArguments().getInt("id"))
                    bundle.putString("image", requireArguments().getString("image"))
                    bundle.putString("name", requireArguments().getString("name"))
                    bundle.putBoolean("isAuth", true)
                    fragment.arguments = bundle
                    communicatorFragmentInterface?.addContentFragment(fragment, true)
                } else {
                    binding.main.snackbar(response.msg)
                }
                dismissDialog()
            } catch (e: MalformedJsonException) {
                dismissDialog()
                binding.main.snackbar(e.message!!)
                e.printStackTrace()
            } catch (e: ApiException) {
                dismissDialog()
                binding.main.snackbar(e.message!!)
                e.printStackTrace()
            } catch (e: NoInternetException) {
                dismissDialog()
                binding.main.snackbar(e.message!!)
                e.printStackTrace()
            }
        }
    }
}