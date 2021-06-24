package com.airposted.bitoronbd.ui.location_set

import com.airposted.bitoronbd.data.db.Location

interface CustomClickListener {
    fun onItemClick(location: Location)
}