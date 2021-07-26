package com.airposted.bohon.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.airposted.bohon.R
import com.airposted.bohon.databinding.FragmentPhoneNumberBinding
import com.airposted.bohon.model.auth.AuthResponse
import com.airposted.bohon.utils.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class PhoneNumberFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel
    private lateinit var binding: FragmentPhoneNumberBinding
    private var communicatorFragmentInterface: AuthCommunicatorFragmentInterface? = null
    private var phone: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhoneNumberBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        binding.toolbar.toolbarTitle.text = getString(R.string.mobile_number)
        binding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
        }
        communicatorFragmentInterface = context as AuthCommunicatorFragmentInterface
        textWatcher(requireContext(), 9, binding.phone, binding.next)
        binding.next.setOnClickListener {
            hideKeyboard(requireActivity())
            phone = zeroRemove(binding.phone.text.toString().trim())
            if (phone!!.length == 10) {
                setProgressDialog(requireContext())
                lifecycleScope.launch {
                    try {
                        val authResponse = viewModel.checkNumber("+880$phone")
                        if (authResponse.success) {
                            if (authResponse.msg == "User not registered") {
                                dismissDialog()
                                val fragment = SignUpFragment()
                                val bundle = Bundle()
                                bundle.putString("phone", "+880$phone")
                                fragment.arguments = bundle
                                communicatorFragmentInterface?.addContentFragment(fragment, true)
                            } else {
                                dismissDialog()
                                val fragment = WelcomeFragment()
                                val bundle = Bundle()
                                bundle.putString("phone", authResponse.user.phone)
                                bundle.putString("token", authResponse.data.token)
                                bundle.putInt("id", authResponse.user.id)
                                bundle.putString("image", authResponse.user.image)
                                bundle.putString("name", authResponse.user.username)
                                bundle.putBoolean("isAuth", true)
                                fragment.arguments = bundle
                                communicatorFragmentInterface?.addContentFragment(fragment, true)
                            }
                        } else {
                            dismissDialog()
                            binding.main.snackbar(authResponse.msg)
                        }
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
            } else {
                binding.main.snackbar("Incorrect Phone Number")
            }
        }
    }
}