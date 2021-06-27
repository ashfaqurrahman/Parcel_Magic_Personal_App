package com.airposted.bohon.ui.auth

import androidx.fragment.app.Fragment

interface AuthCommunicatorFragmentInterface {
    fun addContentFragment(fragment: Fragment?, addToBackStack: Boolean)
}