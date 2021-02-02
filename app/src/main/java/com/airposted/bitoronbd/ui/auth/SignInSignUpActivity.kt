package com.airposted.bitoronbd.ui.auth

import `in`.aabhasjindal.otptextview.OTPListener
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.*
import com.airposted.bitoronbd.ui.MainActivity
import com.airposted.bitoronbd.util.*
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.concurrent.TimeUnit

class SignInSignUpActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var binding: ActivitySignInSignUpBinding
    private lateinit var viewModel: AuthViewModel
    var phone: String? = null
    var timer: CountDownTimer? = null
    private var verificationId: String? = null
    private lateinit var mAuth: FirebaseAuth
    var otp1: String? = null
    var isAvailable = false
    var token:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in_sign_up)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        mAuth = Firebase.auth

        binding.openLayout.main.visibility = View.VISIBLE
        binding.numberLayout.main.visibility = View.GONE
        binding.welcomeBackLayout.main.visibility = View.GONE
        binding.otpLayout.main.visibility = View.GONE
        binding.signUpLayout.main.visibility = View.GONE

        customTextView(binding.openLayout.tvTermsConditionSignup, this)

        binding.openLayout.next.setOnClickListener {
            binding.openLayout.main.visibility = View.GONE
            binding.numberLayout.main.visibility = View.VISIBLE
            binding.welcomeBackLayout.main.visibility = View.GONE
            binding.otpLayout.main.visibility = View.GONE
            binding.signUpLayout.main.visibility = View.GONE

            binding.numberLayout.toolbar.toolbarTitle.text = "Mobile Number"

            textWatcher(this, 9, binding.numberLayout.phone, binding.numberLayout.next)
        }

        binding.numberLayout.next.setOnClickListener {
            hideKeyboard(this)
            setProgressDialog(this)

            phone = zeroRemove(binding.numberLayout.phone.text.toString().trim())
            lifecycleScope.launch {
                try {
                    val authResponse = viewModel.checkNumber("+880$phone")
                    if (authResponse.data != null) {
                        dismissDialog()
                        binding.openLayout.main.visibility = View.GONE
                        binding.numberLayout.main.visibility = View.GONE
                        binding.welcomeBackLayout.main.visibility = View.VISIBLE
                        binding.otpLayout.main.visibility = View.GONE
                        binding.signUpLayout.main.visibility = View.GONE
                        isAvailable = true
                        token = authResponse.data.token

                    } else {
                        dismissDialog()
                        binding.openLayout.main.visibility = View.GONE
                        binding.numberLayout.main.visibility = View.GONE
                        binding.welcomeBackLayout.main.visibility = View.GONE
                        binding.otpLayout.main.visibility = View.GONE
                        binding.signUpLayout.main.visibility = View.VISIBLE

                        binding.signUpLayout.toolbar.toolbarTitle.text = "Sign Up"

                        textWatcher(
                            applicationContext,
                            0,
                            binding.signUpLayout.name,
                            binding.signUpLayout.next
                        )
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

        binding.welcomeBackLayout.next.setOnClickListener {
            binding.openLayout.main.visibility = View.GONE
            binding.numberLayout.main.visibility = View.GONE
            binding.welcomeBackLayout.main.visibility = View.GONE
            binding.otpLayout.main.visibility = View.VISIBLE
            binding.signUpLayout.main.visibility = View.GONE
            binding.otpLayout.verify.isEnabled = false

            binding.otpLayout.otpTopText.text = "Enter verification code sent to\n+880$phone"

            binding.otpLayout.toolbar.toolbarTitle.text = "Verification"

            binding.otpLayout.otpView.otpListener = object : OTPListener {
                override fun onInteractionListener() {
                    binding.otpLayout.verify.isEnabled = false
                    binding.otpLayout.verify.background = ContextCompat.getDrawable(
                        this@SignInSignUpActivity,
                        R.drawable.before_button_bg
                    )
                    binding.otpLayout.otpView.resetState()
                }

                override fun onOTPComplete(otp: String) {
                    otp1 = otp
                    binding.otpLayout.verify.background = ContextCompat.getDrawable(
                        this@SignInSignUpActivity,
                        R.drawable.after_button_bg
                    )
                    binding.otpLayout.verify.isEnabled = true
                }
            }

            timer()
            sendVerificationCode("+880$phone")
        }

        binding.otpLayout.verify.setOnClickListener {
            hideKeyboard(this)
            setProgressDialog(this)
            val code = otp1
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code!!)
            signInWithCredential(credential)
        }

        binding.numberLayout.toolbar.backImage.setOnClickListener {
            hideKeyboard(this)
            binding.openLayout.main.visibility = View.VISIBLE
            binding.numberLayout.main.visibility = View.GONE
            binding.welcomeBackLayout.main.visibility = View.GONE
            binding.otpLayout.main.visibility = View.GONE
            binding.signUpLayout.main.visibility = View.GONE

            binding.numberLayout.phone.setText("")
        }

        binding.welcomeBackLayout.back.setOnClickListener {
            binding.openLayout.main.visibility = View.GONE
            binding.numberLayout.main.visibility = View.VISIBLE
            binding.welcomeBackLayout.main.visibility = View.GONE
            binding.otpLayout.main.visibility = View.GONE
            binding.signUpLayout.main.visibility = View.GONE

            binding.numberLayout.toolbar.toolbarTitle.text = "Mobile Number"
        }

        binding.signUpLayout.toolbar.backImage.setOnClickListener {
            hideKeyboard(this)
            binding.openLayout.main.visibility = View.GONE
            binding.numberLayout.main.visibility = View.VISIBLE
            binding.welcomeBackLayout.main.visibility = View.GONE
            binding.otpLayout.main.visibility = View.GONE
            binding.signUpLayout.main.visibility = View.GONE

            binding.signUpLayout.name.setText("")

            binding.numberLayout.toolbar.toolbarTitle.text = "Mobile Number"
        }

        binding.otpLayout.toolbar.backImage.setOnClickListener {
            hideKeyboard(this)
            binding.openLayout.main.visibility = View.GONE
            binding.numberLayout.main.visibility = View.VISIBLE
            binding.welcomeBackLayout.main.visibility = View.GONE
            binding.otpLayout.main.visibility = View.GONE
            binding.signUpLayout.main.visibility = View.GONE

            timer!!.cancel()
            binding.signUpLayout.name.setText("")
            binding.otpLayout.otpView.setOTP("")

            binding.numberLayout.toolbar.toolbarTitle.text = "Mobile Number"
        }

        binding.signUpLayout.next.setOnClickListener {

            binding.otpLayout.toolbar.toolbarTitle.text = "Verification"

            binding.otpLayout.verify.isEnabled = false

            binding.otpLayout.otpView.otpListener = object : OTPListener {
                override fun onInteractionListener() {
                    binding.otpLayout.verify.isEnabled = false
                    binding.otpLayout.verify.background = ContextCompat.getDrawable(
                        this@SignInSignUpActivity,
                        R.drawable.before_button_bg
                    )
                    binding.otpLayout.otpView.resetState()
                }

                override fun onOTPComplete(otp: String) {
                    otp1 = otp
                    binding.otpLayout.verify.background = ContextCompat.getDrawable(
                        this@SignInSignUpActivity,
                        R.drawable.after_button_bg
                    )
                    binding.otpLayout.verify.isEnabled = true
                }
            }

            hideKeyboard(this)
            binding.openLayout.main.visibility = View.GONE
            binding.numberLayout.main.visibility = View.GONE
            binding.welcomeBackLayout.main.visibility = View.GONE
            binding.otpLayout.main.visibility = View.VISIBLE
            binding.signUpLayout.main.visibility = View.GONE

            binding.otpLayout.otpTopText.text = "Enter verification code sent to\n+880$phone"

            timer()
            sendVerificationCode("+880$phone")

        }

        binding.otpLayout.resend.setOnClickListener {
            timer()
            sendVerificationCode("+880$phone")
        }
    }

    private fun timer() {
        binding.otpLayout.resend.isClickable = false
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.otpLayout.resend.text = " " + (millisUntilFinished / 1000) + "s"
            }

            override fun onFinish() {
                binding.otpLayout.resend.text = " Resend Code"
                binding.otpLayout.resend.isClickable = true
            }
        }
        timer?.start()
    }

    private fun sendVerificationCode(number: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            number,
            60,
            TimeUnit.SECONDS,
            this,
            mCallBack
        )
    }

    private val mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                s: String,
                forceResendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                verificationId = s
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode
                if (code != null) {
                    binding.otpLayout.otpView.setOTP(code)
                    setProgressDialog(this@SignInSignUpActivity)
                    val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                    signInWithCredential(credential)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                dismissDialog()
                binding.rootLayout.snackbar(e.message!!)
            }
        }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    if (isAvailable){
                        dismissDialog()
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_LONG).show()
                        binding.rootLayout.snackbar("Login Successful")
                        PersistentUser.getInstance().setLogin(this)
                        PersistentUser.getInstance().setAccessToken(this, token)
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finish()
                    } else {
                        lifecycleScope.launch {
                            try {
                                val authResponse = viewModel.userSignup(
                                    binding.signUpLayout.name.text.toString(),
                                    "+880$phone"
                                )
                                if (authResponse.success) {
                                    dismissDialog()
                                    Toast.makeText(this@SignInSignUpActivity, authResponse.msg, Toast.LENGTH_LONG).show()
                                    PersistentUser.getInstance().setLogin(this@SignInSignUpActivity)
                                    PersistentUser.getInstance().setAccessToken(this@SignInSignUpActivity, token)
                                    startActivity(
                                        Intent(
                                            applicationContext,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()

                                } else {
                                    dismissDialog()
                                    binding.rootLayout.snackbar(authResponse.msg)
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

                } else {
                    binding.rootLayout.snackbar(task.exception!!.message!!)
                }
            }
    }
}