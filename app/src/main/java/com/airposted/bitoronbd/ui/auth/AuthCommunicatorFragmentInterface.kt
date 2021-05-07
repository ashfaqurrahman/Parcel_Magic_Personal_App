package com.airposted.bitoronbd.ui.auth

import androidx.fragment.app.Fragment

interface AuthCommunicatorFragmentInterface {
    fun addContentFragment(fragment: Fragment?, addToBackStack: Boolean)
}