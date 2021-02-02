package com.airposted.bitoronbd.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aapbd.appbajarlib.storage.PersistData
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.ui.auth.SignInSignUpActivity
import com.airposted.bitoronbd.util.AppHelper

class SplashActivity : AppCompatActivity() {
    private var context: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context = this

        if (PersistData.getBooleanData(context, AppHelper.OPEN_SCREEN_LOAD)) {
            if (PersistentUser.getInstance().isLogged(context)) {
                startActivity(Intent(context, MainActivity::class.java))
                finish()
                //gotoNewActivityWithClearActivity(MainActivity::class.java)
            } else {
                startActivity(Intent(context, SignInSignUpActivity::class.java))
                finish()
                //gotoNewActivityWithClearActivity(SignInSignUpActivity::class.java)
            }
        } else {
            startActivity(Intent(context, IntroActivity::class.java))
            finish()
            //gotoNewActivityWithClearActivity(IntroActivity::class.java)
        }
    }
}