package com.airposted.bohon.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.airposted.bohon.R
import com.airposted.bohon.utils.AppHelper

class WebViewActivity : AppCompatActivity() {
    var url = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val title = findViewById<TextView>(R.id.web_title)
        val webView = findViewById<WebView>(R.id.webView)
        val backImage = findViewById<AppCompatImageView>(R.id.backImage)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val web_title = title as AppCompatTextView

        web_title.text = AppHelper.webviewTitle
        url = intent.getStringExtra(AppHelper.DETAILS_KEY)!!
        webView.loadUrl(url)
        // Enable Javascript
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Force links and redirects to open in the WebView instead of in a browser
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }

        backImage.setOnClickListener {
            finish()
        }
    }
}