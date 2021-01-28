package com.airposted.bitoronbd.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.airposted.bitoronbd.R

class SplashActivity : AppCompatActivity() {
    private var context: Context? = null
    val SPLASH_DISPLAY_LENGTH = 3000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()
        context = this

        Handler().postDelayed({
            startActivity(Intent(context, IntroActivity::class.java))
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}