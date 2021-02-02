package com.airposted.bitoronbd.util

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.ui.WebviewActivity
import com.google.android.material.snackbar.Snackbar


fun Context.toast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun ProgressBar.show(){
    visibility = View.VISIBLE
}

fun ProgressBar.hide(){
    visibility = View.GONE
}

fun View.snackbar(message: String){
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).also { snackbar ->
        snackbar.setAction("Ok") {
            snackbar.dismiss()
        }
    }.show()
}

var pDialog: ProgressDialog? = null

fun setProgressDialog(context: Context) {
    try {
        pDialog = ProgressDialog(context)
        pDialog!!.setCancelable(false)
        pDialog!!.setCanceledOnTouchOutside(false)
        pDialog!!.show()
        pDialog!!.setContentView(R.layout.custom_progress_bar)
    } catch (ignored: Exception) {
    }
}

fun dismissDialog() {
    try {
        if (pDialog != null && pDialog!!.isShowing) {
            pDialog!!.dismiss()
        }
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        pDialog = null
    }
}

fun zeroRemove(phone: String): String {
    var phoneNo = phone
    if (phoneNo.startsWith("0")) {
        phoneNo = phoneNo.substring(1)
    }
    return phoneNo
}

fun hideKeyboard(activity: Activity) {
    val imm: InputMethodManager =
        activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun textWatcher(context: Context, size: Int, input: EditText, button: TextView) {
    button.isEnabled = false
    input.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {

        }

        override fun beforeTextChanged(
            s: CharSequence?, start: Int,
            count: Int, after: Int
        ) {
        }

        override fun onTextChanged(
            s: CharSequence, start: Int,
            before: Int, count: Int
        ) {
            if (s.length > size) {
                button.background = ContextCompat.getDrawable(context, R.drawable.after_button_bg)
                button.isEnabled = true
            } else {
                button.background = ContextCompat.getDrawable(context, R.drawable.before_button_bg)
                button.isEnabled = false
            }
        }
    })
}

fun customTextView(view: TextView, context: Context) {
    val spanTxt = SpannableStringBuilder(
        context.getString(R.string.by_useing)
    )
    spanTxt.append(" ").append(context.getString(R.string.terms_of_service))
    spanTxt.setSpan(object : ClickableSpan() {
        override fun onClick(widget: View) {
            AppHelper.webviewTitle = context.getString(R.string.terms_of_service)
            val bundle = Bundle()
            bundle.putString(AppHelper.DETAILS_KEY, AppHelper.TERMS)
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
        }
    }, spanTxt.length - context.getString(R.string.terms_of_service).length, spanTxt.length, 0)
    spanTxt.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue
        )),
        context.getString(R.string.by_useing).length,
        spanTxt.length,
        0
    )
    spanTxt.append(" ").append(context.getString(R.string.and))
    spanTxt.append(" ").append(context.getString(R.string.privacy_policy))
    spanTxt.setSpan(object : ClickableSpan() {
        override fun onClick(widget: View) {
            AppHelper.webviewTitle = context.getString(R.string.privacy_policy)
            val bundle = Bundle()
            bundle.putString(AppHelper.DETAILS_KEY, AppHelper.PEIVACY_POLICY)
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
        }
    }, spanTxt.length - context.getString(R.string.privacy_policy).length, spanTxt.length, 0)
    view.movementMethod = LinkMovementMethod.getInstance()
    spanTxt.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue
        )),
        context.getString(R.string.by_useing).length + context.getString(R.string.and).length + context.getString(R.string.terms_of_service).length + 2,
        spanTxt.length,
        0
    )
    view.setText(spanTxt, TextView.BufferType.SPANNABLE)
}
