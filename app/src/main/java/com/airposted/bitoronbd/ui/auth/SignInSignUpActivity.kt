package com.airposted.bitoronbd.ui.auth

import `in`.aabhasjindal.otptextview.OTPListener
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.R.string.enter
import com.airposted.bitoronbd.data.network.responses.AuthResponse
import com.airposted.bitoronbd.databinding.*
import com.airposted.bitoronbd.ui.permission.PermissionActivity
import com.airposted.bitoronbd.utils.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.io.File
import java.util.concurrent.TimeUnit


class SignInSignUpActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var binding: ActivitySignInSignUpBinding
    private lateinit var viewModel: AuthViewModel
    var phone: String? = null
    private var timer: CountDownTimer? = null
    private var verificationId: String? = null
    private lateinit var mAuth: FirebaseAuth
    var otp1: String? = null
    private var isAvailable = false
    private var authResponse: AuthResponse? = null
    private var cropImageUri: Uri? = null

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

            binding.numberLayout.toolbar.toolbarTitle.text = getString(R.string.mobile_number)

            textWatcher(this, 9, binding.numberLayout.phone, binding.numberLayout.next)
        }

        binding.numberLayout.next.setOnClickListener {
            hideKeyboard(this)
            setProgressDialog(this)

            phone = zeroRemove(binding.numberLayout.phone.text.toString().trim())
            lifecycleScope.launch {
                try {
                    authResponse = viewModel.checkNumber("+880$phone")
                    if (authResponse?.data != null) {
                        dismissDialog()
                        binding.openLayout.main.visibility = View.GONE
                        binding.numberLayout.main.visibility = View.GONE
                        binding.welcomeBackLayout.main.visibility = View.VISIBLE
                        binding.otpLayout.main.visibility = View.GONE
                        binding.signUpLayout.main.visibility = View.GONE
                        isAvailable = true

                    } else {
                        dismissDialog()
                        binding.openLayout.main.visibility = View.GONE
                        binding.numberLayout.main.visibility = View.GONE
                        binding.welcomeBackLayout.main.visibility = View.GONE
                        binding.otpLayout.main.visibility = View.GONE
                        binding.signUpLayout.main.visibility = View.VISIBLE

                        binding.signUpLayout.toolbar.toolbarTitle.text = getString(R.string.sign_up)

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

        binding.numberLayout.toolbar.backImage.setOnClickListener {
            hideKeyboard(this)
            binding.openLayout.main.visibility = View.VISIBLE
            binding.numberLayout.main.visibility = View.GONE
            binding.welcomeBackLayout.main.visibility = View.GONE
            binding.otpLayout.main.visibility = View.GONE
            binding.signUpLayout.main.visibility = View.GONE

            binding.numberLayout.phone.setText("")
        }

        binding.welcomeBackLayout.next.setOnClickListener {
            binding.openLayout.main.visibility = View.GONE
            binding.numberLayout.main.visibility = View.GONE
            binding.welcomeBackLayout.main.visibility = View.GONE
            binding.otpLayout.main.visibility = View.VISIBLE
            binding.signUpLayout.main.visibility = View.GONE
            binding.otpLayout.verify.isEnabled = false

            binding.otpLayout.otpTopText.text = getString(enter, phone)

            binding.otpLayout.toolbar.toolbarTitle.text = getString(R.string.verification)

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

        binding.welcomeBackLayout.back.setOnClickListener {
            binding.openLayout.main.visibility = View.GONE
            binding.numberLayout.main.visibility = View.VISIBLE
            binding.welcomeBackLayout.main.visibility = View.GONE
            binding.otpLayout.main.visibility = View.GONE
            binding.signUpLayout.main.visibility = View.GONE

            isAvailable = false

            binding.numberLayout.toolbar.toolbarTitle.text = getString(R.string.mobile_number)
        }

        binding.otpLayout.verify.setOnClickListener {
            hideKeyboard(this)
            setProgressDialog(this)
            val code = otp1
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code!!)
            signInWithCredential(credential)
        }

        binding.otpLayout.resend.setOnClickListener {
            timer()
            sendVerificationCode("+880$phone")
        }

        binding.otpLayout.toolbar.backImage.setOnClickListener {
            hideKeyboard(this)
            binding.openLayout.main.visibility = View.GONE
            binding.numberLayout.main.visibility = View.VISIBLE
            binding.welcomeBackLayout.main.visibility = View.GONE
            binding.otpLayout.main.visibility = View.GONE
            binding.signUpLayout.main.visibility = View.GONE

            isAvailable = false
            cropImageUri = null
            binding.signUpLayout.profileImage.setImageResource(R.drawable.sample_pro_pic)

            timer!!.cancel()
            binding.signUpLayout.name.setText("")
            binding.otpLayout.otpView.setOTP("")

            binding.numberLayout.toolbar.toolbarTitle.text = getString(R.string.mobile_number)
        }

        binding.signUpLayout.next.setOnClickListener {

            binding.otpLayout.toolbar.toolbarTitle.text = getString(R.string.verification)

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

            binding.otpLayout.otpTopText.text = getString(enter, phone)

            timer()
            sendVerificationCode("+880$phone")

        }

        binding.signUpLayout.imageUpload.setOnClickListener {
            CropImage.startPickImageActivity(this)
        }

        binding.signUpLayout.toolbar.backImage.setOnClickListener {
            hideKeyboard(this)
            binding.openLayout.main.visibility = View.GONE
            binding.numberLayout.main.visibility = View.VISIBLE
            binding.welcomeBackLayout.main.visibility = View.GONE
            binding.otpLayout.main.visibility = View.GONE
            binding.signUpLayout.main.visibility = View.GONE

            isAvailable = false
            cropImageUri = null
            binding.signUpLayout.profileImage.setImageResource(R.drawable.sample_pro_pic)

            binding.signUpLayout.name.setText("")

            binding.numberLayout.toolbar.toolbarTitle.text = getString(R.string.mobile_number)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = CropImage.getPickImageResultUri(this, data)
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                cropImageUri = imageUri
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
            } else {
                startCropImageActivity(imageUri)
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                binding.signUpLayout.profileImage.setImageURI(result.uri)
                cropImageUri = result.uri
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                toast("Cropping failed: " + result.error)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (cropImageUri != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCropImageActivity(cropImageUri!!)
        } else {
            toast("Cancelling, required permissions are not granted")
        }
    }

    private fun startCropImageActivity(imageUri: Uri?) {
        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .setAspectRatio(1, 1)
            .start(this)
    }


    private fun timer() {
        binding.otpLayout.resend.isClickable = false
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                " ${millisUntilFinished / 1000}s".also { binding.otpLayout.resend.text = it }
            }

            override fun onFinish() {
                binding.otpLayout.resend.text = getString(R.string.resend_code)
                binding.otpLayout.resend.isClickable = true
            }
        }
        timer?.start()
    }

    private fun sendVerificationCode(number: String) {

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBack)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
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
                        toast("Login Successful")
                        //binding.rootLayout.snackbar("Login Successful")
                        PersistentUser.getInstance().setLogin(this)
                        PersistentUser.getInstance().setAccessToken(this, "Bearer " + authResponse?.data?.token)
                        PersistentUser.getInstance().setUserID(
                            this,
                            authResponse?.user?.id.toString()
                        )
                        PersistentUser.getInstance().setFullname(this, authResponse?.user?.name)
                        PersistentUser.getInstance().setPhonenumber(this, authResponse?.user?.phone)
                        PersistentUser.getInstance().setUserImage(this, authResponse?.user?.image)
                        startActivity(Intent(applicationContext, PermissionActivity::class.java))
                        finish()
                    } else {
                        lifecycleScope.launch {
                            try {
                                val signupResponse: AuthResponse?
                                if (cropImageUri != null){
                                    val file = File(cropImageUri?.path)
                                    val compressedImage = reduceImageSize(file)
                                    if (compressedImage != null){
                                        val fileReqBody = RequestBody.create(
                                            MediaType.parse("image/*"),
                                            compressedImage
                                        )
                                        val part = MultipartBody.Part.createFormData(
                                            "image",
                                            compressedImage.name,
                                            fileReqBody
                                        )
                                        val photoName = RequestBody.create(
                                            MediaType.parse("text/plain"),
                                            "image-type"
                                        )
                                        val name = RequestBody.create(
                                            MediaType.parse("text/plain"),
                                            binding.signUpLayout.name.text.toString()
                                        )
                                        val phone = RequestBody.create(
                                            MediaType.parse("text/plain"),
                                            "+880$phone"
                                        )
                                        signupResponse = viewModel.userSignupWithPhoto(
                                            name,
                                            phone,
                                            part,
                                            photoName
                                        )
                                    } else {
                                        val fileReqBody = RequestBody.create(
                                            MediaType.parse("image/*"),
                                            file
                                        )
                                        val part = MultipartBody.Part.createFormData(
                                            "image",
                                            file.name,
                                            fileReqBody
                                        )
                                        val photoName = RequestBody.create(
                                            MediaType.parse("text/plain"),
                                            "image-type"
                                        )
                                        val name = RequestBody.create(
                                            MediaType.parse("text/plain"),
                                            binding.signUpLayout.name.text.toString()
                                        )
                                        val phone = RequestBody.create(
                                            MediaType.parse("text/plain"),
                                            "+880$phone"
                                        )
                                        signupResponse = viewModel.userSignupWithPhoto(
                                            name,
                                            phone,
                                            part,
                                            photoName
                                        )
                                    }

                                } else {
                                    signupResponse = viewModel.userSignup(
                                        binding.signUpLayout.name.text.toString(),
                                        "+880$phone"
                                    )
                                }
                                if (signupResponse.success) {
                                    dismissDialog()
                                    toast(signupResponse.msg)
                                    PersistentUser.getInstance().setLogin(this@SignInSignUpActivity)
                                    PersistentUser.getInstance().setAccessToken(
                                        this@SignInSignUpActivity,
                                        "Bearer " + signupResponse.data?.token
                                    )
                                    PersistentUser.getInstance().setUserID(
                                        this@SignInSignUpActivity,
                                        signupResponse.user?.id.toString()
                                    )
                                    PersistentUser.getInstance().setFullname(
                                        this@SignInSignUpActivity,
                                        signupResponse.user?.name
                                    )
                                    PersistentUser.getInstance().setPhonenumber(
                                        this@SignInSignUpActivity,
                                        signupResponse.user?.phone
                                    )
                                    PersistentUser.getInstance().setUserImage(
                                        this@SignInSignUpActivity,
                                        signupResponse.user?.image
                                    )
                                    startActivity(
                                        Intent(
                                            applicationContext,
                                            PermissionActivity::class.java
                                        )
                                    )
                                    finish()

                                } else {
                                    dismissDialog()
                                    binding.rootLayout.snackbar(signupResponse.msg)
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