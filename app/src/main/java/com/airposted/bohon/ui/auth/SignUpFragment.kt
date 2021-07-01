package com.airposted.bohon.ui.auth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.MalformedJsonException
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.airposted.bohon.R
import com.airposted.bohon.databinding.FragmentSignUpBinding
import com.airposted.bohon.utils.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class SignUpFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var viewModel: AuthViewModel
    private val factory: AuthViewModelFactory by instance()
    private lateinit var binding: FragmentSignUpBinding
    private var communicatorFragmentInterface: AuthCommunicatorFragmentInterface? = null
    private var mCropImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        binding.toolbar.toolbarTitle.text = getString(R.string.sign_up)
        binding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
        }
        communicatorFragmentInterface = context as AuthCommunicatorFragmentInterface
        binding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.imageUpload.setOnClickListener {
            uploadImage()
        }
        textWatcher(requireContext(), 0, binding.name, binding.next)
        binding.next.setOnClickListener {
            hideKeyboard(requireActivity())
            val name = binding.name.text.toString()
            if (name.isNotEmpty()) {
                sendOTP()
//                val fragment = OTPFragment()
//                val bundle = Bundle()
//                bundle.putString("imageUri", mCropImageUri?.path)
//                bundle.putString("phone", requireArguments().getString("phone"))
//                bundle.putString("name", binding.name.text.toString())
//                bundle.putBoolean("isAuth", false)
//                fragment.arguments = bundle
//                communicatorFragmentInterface?.addContentFragment(fragment, true)
            } else {
                binding.main.snackbar("Username is required")
            }
//            if (mCropImageUri != null) {
//
//            } else {
//                binding.main.snackbar("User photo is required")
//            }
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
                    bundle.putString("imageUri", mCropImageUri?.path)
                    bundle.putString("phone", requireArguments().getString("phone"))
                    bundle.putString("name", binding.name.text.toString())
                    bundle.putBoolean("isAuth", false)
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

    private fun uploadImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            } else {
                imagePick()
            }
        } else {
            imagePick()
        }
    }

    private fun imagePick() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .start(requireContext(), this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                mCropImageUri = result.uri
                binding.profileImage.setImageURI(mCropImageUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }
}