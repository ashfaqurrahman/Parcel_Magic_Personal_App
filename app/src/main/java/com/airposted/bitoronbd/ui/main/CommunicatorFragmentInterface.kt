package com.airposted.bitoronbd.ui.main

import androidx.fragment.app.Fragment

interface CommunicatorFragmentInterface {
    fun addContentFragment(fragment: Fragment?, addToBackStack: Boolean)
}