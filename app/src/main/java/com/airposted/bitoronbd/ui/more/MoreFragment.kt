package com.airposted.bitoronbd.ui.more

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.MalformedJsonException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentMoreBinding
import com.airposted.bitoronbd.ui.auth.SignInSignUpActivity
import com.airposted.bitoronbd.ui.permission.PermissionActivity
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


class MoreFragment : Fragment(), KodeinAware {
    private lateinit var moreBinding: FragmentMoreBinding
    override val kodein by kodein()

    private val factory: MoreViewModelFactory by instance()
    private lateinit var viewModel: MoreViewModel
    var edit = false
    private var cropImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        moreBinding = FragmentMoreBinding.inflate(inflater, container, false)

        return moreBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), factory).get(MoreViewModel::class.java)
        bindUI()
    }

    private fun bindUI() = Coroutines.main {
        moreBinding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }
        Glide.with(requireActivity()).load(
            PersistentUser.getInstance().getUserImage(requireActivity())
        ).placeholder(R.drawable.sample_pro_pic).error(
            R.drawable.sample_pro_pic
        ).into(moreBinding.profileImage)

        moreBinding.singOut.setOnClickListener {
            PersistentUser.getInstance().logOut(context)
            val intent = Intent(requireContext(), SignInSignUpActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        viewModel.getName.await().observe(requireActivity(), {
            moreBinding.profileName.text = it
        })

        if (moreBinding.profileName.text.toString().isEmpty()) {
            moreBinding.profileName.text = "No Name"
        }

        moreBinding.phone.text = PersistentUser.getInstance().getPhoneNumber(requireActivity())

        moreBinding.editName.setOnClickListener {
            if (!edit) {
                moreBinding.editProfileName.setText(
                    PersistentUser.getInstance().getFullName(requireActivity())
                )
                moreBinding.profileName.visibility = View.GONE
                moreBinding.editProfileName.visibility = View.VISIBLE
                moreBinding.editProfileName.requestFocus()
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(moreBinding.editProfileName, InputMethodManager.SHOW_IMPLICIT)
                moreBinding.editName.setImageResource(R.drawable.ic_done)
                edit = true
            } else {

                hideKeyboard(requireActivity())
                if (moreBinding.editProfileName.text.toString() != "" && moreBinding.editProfileName.text.toString() != PersistentUser.getInstance()
                        .getFullName(requireActivity())
                ) {
                    setProgressDialog(requireActivity())
                    lifecycleScope.launch {
                        try {
                            val response = viewModel.userNameUpdate(
                                PersistentUser.getInstance().getAccessToken(
                                    requireActivity()
                                ), moreBinding.editProfileName.text.toString()
                            )
                            dismissDialog()
                            moreBinding.profileName.text =
                                moreBinding.editProfileName.text.toString()
                            PersistentUser.getInstance().setFullname(
                                requireContext(),
                                moreBinding.editProfileName.text.toString()
                            )
                            moreBinding.profileName.visibility = View.VISIBLE
                            moreBinding.editProfileName.visibility = View.GONE
                            moreBinding.editName.setImageResource(R.drawable.ic_edit)
                            edit = false
                            moreBinding.rootLayout.snackbar(response)
                        } catch (e: MalformedJsonException) {
                            dismissDialog()
                            moreBinding.rootLayout.snackbar(e.message!!)
                            e.printStackTrace()
                        } catch (e: ApiException) {
                            dismissDialog()
                            moreBinding.rootLayout.snackbar(e.message!!)
                            e.printStackTrace()
                        } catch (e: NoInternetException) {
                            dismissDialog()
                            moreBinding.rootLayout.snackbar(e.message!!)
                            e.printStackTrace()
                        }
                    }
                } else {
                    moreBinding.profileName.visibility = View.VISIBLE
                    moreBinding.editProfileName.visibility = View.GONE
                    moreBinding.editName.setImageResource(R.drawable.ic_edit)
                    edit = false
                }
            }
        }

        moreBinding.imageUpload.setOnClickListener {
            uploadImage()
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
                cropImageUri = result.uri
                setProgressDialog(requireActivity())
                lifecycleScope.launch {
                    try {
                        val file = File(cropImageUri!!.path)
                        val compressedImage = reduceImageSize(file)
                        val fileReqBody = RequestBody.create(
                            MediaType.parse("image/*"),
                            compressedImage!!
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
                        val response = viewModel.userImageUpdate(
                            PersistentUser.getInstance().getAccessToken(
                                requireActivity()
                            ), part, photoName
                        )
                        if (response.success) {
                            PersistentUser.getInstance().setUserImage(
                                requireActivity(),
                                response.user?.image
                            )
                            moreBinding.profileImage.setImageURI(cropImageUri)
                            PersistentUser.getInstance()
                                .setUserImage(requireContext(), cropImageUri!!.path.toString())
                            dismissDialog()
                            moreBinding.rootLayout.snackbar(response.msg)
                        } else {
                            moreBinding.rootLayout.snackbar("Update failed")
                        }
                    } catch (e: MalformedJsonException) {
                        dismissDialog()
                        moreBinding.rootLayout.snackbar(e.message!!)
                        e.printStackTrace()
                    } catch (e: ApiException) {
                        dismissDialog()
                        moreBinding.rootLayout.snackbar(e.message!!)
                        e.printStackTrace()
                    } catch (e: NoInternetException) {
                        dismissDialog()
                        moreBinding.rootLayout.snackbar(e.message!!)
                        e.printStackTrace()
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                moreBinding.main.snackbar(error.toString())
            }
        }
    }
}