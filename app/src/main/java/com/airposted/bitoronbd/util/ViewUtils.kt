package com.airposted.bitoronbd.util

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import com.airposted.bitoronbd.R
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
