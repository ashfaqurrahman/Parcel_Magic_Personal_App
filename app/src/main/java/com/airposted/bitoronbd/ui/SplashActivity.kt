package com.airposted.bitoronbd.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.aapbd.appbajarlib.storage.PersistData
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.ui.auth.AuthActivity
import com.airposted.bitoronbd.ui.main.MainActivity
import com.airposted.bitoronbd.ui.permission.PermissionActivity
import com.airposted.bitoronbd.utils.AppHelper


class SplashActivity : AppCompatActivity() {

    private var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context = this

        if (PersistData.getBooleanData(context, AppHelper.OPEN_SCREEN_LOAD)) {
            if (PersistentUser.getInstance().isLogged(context)) {
                if (checkPermissions()) {
                    startActivity(Intent(context, MainActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(context, PermissionActivity::class.java))
                    finish()
                }
            } else {
                startActivity(Intent(context, AuthActivity::class.java))
                finish()
            }
        } else {
            startActivity(Intent(context, IntroActivity::class.java))
            finish()
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }
}