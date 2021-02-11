package com.airposted.bitoronbd.ui

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aapbd.appbajarlib.storage.PersistData
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.ui.auth.SignInSignUpActivity
import com.airposted.bitoronbd.ui.main.MainActivity
import com.airposted.bitoronbd.utils.AppHelper
import com.ckdroid.dynamicpermissions.PermissionStatus
import com.ckdroid.dynamicpermissions.PermissionUtils

class SplashActivity : AppCompatActivity() {

    private var context: Context? = null
    private val REQUEST_PERMISSION_CODE = 12
    private val CODE = 10
    val requestPermissionList: MutableList<String> = mutableListOf()
    private lateinit var dialogs: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context = this

        initializePermissionList()
        checkPermissions()
    }

    private fun initializePermissionList() {
        requestPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun requestPermissions() {

        dialogs.dismiss()

        PermissionUtils.checkAndRequestPermissions(
            this,
            requestPermissionList,
            REQUEST_PERMISSION_CODE,
            checkStatusOnly = false
        )
    }

    private fun checkPermissions() {

        val permissionResult =
            PermissionUtils.checkAndRequestPermissions(
                this,
                requestPermissionList,
                REQUEST_PERMISSION_CODE,
                checkStatusOnly = true
            )

        when (permissionResult.finalStatus) {
            PermissionStatus.ALLOWED -> {
                if (PersistData.getBooleanData(context, AppHelper.OPEN_SCREEN_LOAD)) {
                    if (PersistentUser.getInstance().isLogged(context)) {
                        startActivity(Intent(context, MainActivity::class.java))
                        finish()
                    } else {
                        startActivity(Intent(context, SignInSignUpActivity::class.java))
                        finish()
                    }
                } else {
                    startActivity(Intent(context, IntroActivity::class.java))
                    finish()
                }
            }
            PermissionStatus.NOT_GIVEN -> {
                permissionDialog()
            }
            PermissionStatus.DENIED_PERMANENTLY -> {
                permissionDialog1()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            //Check status after user allowed or denied permission using the same way while requested permission
            val permissionResult =
                PermissionUtils.checkAndRequestPermissions(
                    this,
                    requestPermissionList,
                    REQUEST_PERMISSION_CODE,
                    checkStatusOnly = true
                )

            when (permissionResult.finalStatus) {
                PermissionStatus.ALLOWED -> {//DO further stuffs as all permissions are allowed by user
                    if (PersistData.getBooleanData(context, AppHelper.OPEN_SCREEN_LOAD)) {
                        if (PersistentUser.getInstance().isLogged(context)) {
                            startActivity(Intent(context, MainActivity::class.java))
                            finish()
                        } else {
                            startActivity(Intent(context, SignInSignUpActivity::class.java))
                            finish()
                        }
                    } else {
                        startActivity(Intent(context, IntroActivity::class.java))
                        finish()
                    }
                }
                PermissionStatus.NOT_GIVEN -> {
                    permissionDialog()
                }
                PermissionStatus.DENIED_PERMANENTLY -> {
                    permissionDialog1()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE) {
            checkPermissions()
        }
    }

    private fun permissionDialog() {
        dialogs = Dialog(this)
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(R.layout.dialog_gps_lost)
        dialogs.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogs.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val tryAgain = dialogs.findViewById<TextView>(R.id.try_again)

        tryAgain.setOnClickListener {
            requestPermissions()
        }

        dialogs.setCancelable(false)

        dialogs.show()
    }

    private fun permissionDialog1() {
        dialogs = Dialog(this)
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(R.layout.dialog_gps_lost)
        dialogs.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogs.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val tryAgain = dialogs.findViewById<TextView>(R.id.try_again)

        tryAgain.setOnClickListener {
            dialogs.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivityForResult(intent, CODE)
        }

        dialogs.setCancelable(false)

        dialogs.show()
    }
}