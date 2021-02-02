package com.airposted.bitoronbd.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.*
import com.airposted.bitoronbd.ui.MainActivity
import com.airposted.bitoronbd.util.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class SignInSignUpActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var binding: ActivitySignInSignUpBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in_sign_up)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        binding.numberLayout.next.isEnabled = false
        binding.numberLayout.phone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.length > 9) {
                    binding.numberLayout.next.background =
                        resources.getDrawable(R.drawable.after_button_bg)
                    binding.numberLayout.next.isEnabled = true
                } else {
                    binding.numberLayout.next.background =
                        resources.getDrawable(R.drawable.before_button_bg)
                    binding.numberLayout.next.isEnabled = false
                }
            }
        })

        binding.openLayout.main.visibility = View.VISIBLE
        binding.numberLayout.main.visibility = View.GONE
        binding.otpLayout.main.visibility = View.GONE
        binding.signUpLayout.main.visibility = View.GONE

        binding.openLayout.next.setOnClickListener {
            binding.openLayout.main.visibility = View.GONE
            binding.numberLayout.main.visibility = View.VISIBLE
            binding.otpLayout.main.visibility = View.GONE
            binding.signUpLayout.main.visibility = View.GONE
        }

        binding.numberLayout.next.setOnClickListener {
            hideKeyboard(this)
            setProgressDialog(this)

            val phone = zeroRemove(binding.numberLayout.phone.text.toString().trim())
            lifecycleScope.launch {
                try {
                    val authResponse = viewModel.checkNumber("+880$phone")
                    if (authResponse.data != null) {
                        dismissDialog()
                        binding.rootLayout.snackbar(authResponse.data.token)
                        //viewModel.saveLoggedInUser(authResponse.user)
                        binding.openLayout.main.visibility = View.GONE
                        binding.numberLayout.main.visibility = View.GONE
                        binding.otpLayout.main.visibility = View.VISIBLE
                        binding.signUpLayout.main.visibility = View.GONE
                    } else {
                        dismissDialog()
                        binding.openLayout.main.visibility = View.GONE
                        binding.numberLayout.main.visibility = View.GONE
                        binding.otpLayout.main.visibility = View.GONE
                        binding.signUpLayout.main.visibility = View.VISIBLE
                    }
                } catch (e: ApiException) {
                    dismissDialog()
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    dismissDialog()
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                }
            }
        }

        binding.otpLayout.verify.setOnClickListener {
            binding.openLayout.main.visibility = View.GONE
            binding.numberLayout.main.visibility = View.GONE
            binding.otpLayout.main.visibility = View.GONE
            binding.signUpLayout.main.visibility = View.VISIBLE
        }

        binding.signUpLayout.next.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}