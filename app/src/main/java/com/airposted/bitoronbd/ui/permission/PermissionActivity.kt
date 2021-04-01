package com.airposted.bitoronbd.ui.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.airposted.bitoronbd.databinding.ActivityPermissionBinding
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import java.util.*
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.ui.main.MainActivity

class PermissionActivity : AppCompatActivity() {

    private lateinit var permissionBinding: ActivityPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionBinding = DataBindingUtil.setContentView(this, R.layout.activity_permission)
    }

    override fun onResume() {
        super.onResume()
        setupUI()
    }

    private fun setupUI() {
        //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_right)

        permissionBinding.currentLocation.setOnClickListener {
            Permissions.check(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
                null,
                object : PermissionHandler() {
                    override fun onGranted() {
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }

                    override fun onDenied(
                        context: Context?,
                        deniedPermissions: ArrayList<String>?
                    ) {
                        super.onDenied(context, deniedPermissions)
                    }

                    override fun onBlocked(
                        context: Context?,
                        blockedList: ArrayList<String>?
                    ): Boolean {
                        return super.onBlocked(context, blockedList)
                    }
                })
        }
    }
}