package com.airposted.bitoronbd.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager

open class BaseActivity: AppCompatActivity() {

     fun zeroRemove(phoneNo: String): String {
        var phoneNo = phoneNo
        if (phoneNo.startsWith("0")) {
            phoneNo = phoneNo.substring(1)
        }
        if (phoneNo.startsWith("880")) {
            phoneNo = phoneNo.substring(3)
        }
        return phoneNo
    }

    protected fun gotoNewActivity(activityClass: Class<*>) {
        if (this.javaClass != activityClass) {
            val intent = Intent(this, activityClass)
            startActivity(intent)
        }

    }

    fun gotoNewActivityWithClearActivity(activityClass: Class<*>) {
        if (this.javaClass != activityClass) {
            val intent = Intent(this, activityClass)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK
                        or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    protected fun gotoNewActivityWithClearActivity(activityClass: Class<*>, bundle: Bundle) {
        if (this.javaClass != activityClass) {
            val intent = Intent(this, activityClass)
            intent.putExtras(bundle)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

    }

    protected fun gotoNewActivityWithOutClearActivity(activityClass: Class<*>, bundle: Bundle) {
        if (this.javaClass != activityClass) {
                val intent = Intent(this, activityClass)
                intent.putExtras(bundle)
                startActivity(intent)
        }

    }

    fun clearBackStackInclusive(tag: String) {
        supportFragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus
        if (view != null) {
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }
}