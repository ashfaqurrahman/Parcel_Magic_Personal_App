package com.airposted.bitoronbd.ui.location_set

import com.airposted.bitoronbd.model.Prediction

interface CustomClickListener {
    fun onItemClick(location: Prediction)
}