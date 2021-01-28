package com.airposted.bitoronbd.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.airposted.bitoronbd.R

class SignInSignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in_sign_up)

        val lay1 = findViewById<RelativeLayout>(R.id.open_layout)
        val lay2 = findViewById<RelativeLayout>(R.id.number_layout)
        val lay3 = findViewById<RelativeLayout>(R.id.otp_layout)
        val lay4 = findViewById<LinearLayout>(R.id.sign_up_layout)

        lay1.visibility = View.VISIBLE
        lay2.visibility = View.GONE
        lay3.visibility = View.GONE
        lay4.visibility = View.GONE
    }
}