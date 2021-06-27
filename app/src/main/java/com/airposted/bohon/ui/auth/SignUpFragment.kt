package com.airposted.bohon.ui.auth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airposted.bohon.R
import com.airposted.bohon.databinding.FragmentSignUpBinding
import com.airposted.bohon.utils.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding
    private var communicatorFragmentInterface: AuthCommunicatorFragmentInterface? = null
    private var mCropImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
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
                val fragment = OTPFragment()
                val bundle = Bundle()
                bundle.putString("imageUri", mCropImageUri?.path)
                bundle.putString("phone", requireArguments().getString("phone"))
                bundle.putString("name", binding.name.text.toString())
                bundle.putBoolean("isAuth", false)
                fragment.arguments = bundle
                communicatorFragmentInterface?.addContentFragment(fragment, true)
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