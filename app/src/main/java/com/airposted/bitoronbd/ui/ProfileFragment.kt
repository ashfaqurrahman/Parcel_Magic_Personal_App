package com.airposted.bitoronbd.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.MalformedJsonException
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentProfileBinding
import com.airposted.bitoronbd.ui.setting.SettingViewModel
import com.airposted.bitoronbd.ui.setting.SettingViewModelFactory
import com.airposted.bitoronbd.utils.*
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.io.File
import java.util.*

class ProfileFragment : Fragment(), KodeinAware {

    private lateinit var profileBinding: FragmentProfileBinding
    override val kodein by kodein()

    private val factory: SettingViewModelFactory by instance()
    private lateinit var viewModel: SettingViewModel
    private var cropImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        profileBinding = FragmentProfileBinding.inflate(inflater, container, false)

        return profileBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), factory).get(SettingViewModel::class.java)
        bindUI()
    }

    private fun bindUI() = Coroutines.main {
        profileBinding.toolbar.toolbarTitle.text = getString(R.string.my_profile)

        profileBinding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
        }

        profileBinding.imageUpload.setOnClickListener {
            CropImage.startPickImageActivity(requireContext(), this)
        }

        profileBinding.txtEditProfile.setOnClickListener {
            showProfileDialogue()
        }

        viewModel.getName.await().observe(requireActivity(), {
            profileBinding.tvName.text = it
        })

        viewModel.getImage.await().observe(requireActivity(), {
            Glide.with(requireActivity()).load(it).placeholder(R.drawable.sample_pro_pic).error(
                R.drawable.sample_pro_pic
            ).into(profileBinding.profileImage)
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = CropImage.getPickImageResultUri(requireActivity(), data)
            if (CropImage.isReadExternalStoragePermissionsRequired(requireActivity(), imageUri)) {
                cropImageUri = imageUri
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
            } else {
                startCropImageActivity(imageUri)
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                profileBinding.profileImage.setImageURI(result.uri)
                cropImageUri = result.uri
                setProgressDialog(requireActivity())
                lifecycleScope.launch {
                    try {
                        val file = File(cropImageUri?.path)
                        val compressedImage = reduceImageSize(file)
                        val fileReqBody = RequestBody.create(
                            MediaType.parse("image/*"),
                            compressedImage
                        )
                        val part = MultipartBody.Part.createFormData(
                            "image",
                            compressedImage?.name,
                            fileReqBody
                        )
                        val photoName = RequestBody.create(
                            MediaType.parse("text/plain"),
                            "image-type"
                        )
                        val response = viewModel.userImageUpdate("Bearer " + PersistentUser.getInstance().getAccessToken(requireActivity()),part, photoName)
                        dismissDialog()
                        profileBinding.rootLayout.snackbar(response)
                    } catch (e: MalformedJsonException) {
                        dismissDialog()
                        profileBinding.rootLayout.snackbar(e.message!!)
                        e.printStackTrace()
                    } catch (e: ApiException) {
                        dismissDialog()
                        profileBinding.rootLayout.snackbar(e.message!!)
                        e.printStackTrace()
                    } catch (e: NoInternetException) {
                        dismissDialog()
                        profileBinding.rootLayout.snackbar(e.message!!)
                        e.printStackTrace()
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                requireActivity().toast("Cropping failed: " + result.error)
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
            requireActivity().toast("Cancelling, required permissions are not granted")
        }
    }

    private fun startCropImageActivity(imageUri: Uri?) {
        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .setAspectRatio(1, 1)
            .start(requireContext(), this)
    }

    private fun showProfileDialogue() {
        val dialogs = Dialog(requireActivity())
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(R.layout.update_profile)
        Objects.requireNonNull(dialogs.window)
            ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogs.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val etName = dialogs.findViewById(R.id.et_name) as AppCompatEditText
        val buttonClose = dialogs.findViewById(R.id.button_close) as ImageView

        buttonClose.setOnClickListener {
            dialogs.dismiss()
        }

        val btnUpdate = dialogs.findViewById(R.id.btnUpdate) as AppCompatButton

        btnUpdate.setOnClickListener {
            hideKeyboard(requireActivity())
            setProgressDialog(requireActivity())
            lifecycleScope.launch {
                try {
                    val response = viewModel.userNameUpdate("Bearer " + PersistentUser.getInstance().getAccessToken(requireActivity()),etName.text.toString())
                    dialogs.dismiss()
                    dismissDialog()
                    profileBinding.rootLayout.snackbar(response)
                } catch (e: MalformedJsonException) {
                    dismissDialog()
                    profileBinding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                } catch (e: ApiException) {
                    dismissDialog()
                    profileBinding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    dismissDialog()
                    profileBinding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                }
            }
        }

        dialogs.show()
    }

}