package com.airposted.bohon.ui.location_set

import com.airposted.bohon.data.db.Location

interface CustomClickListener {
    fun onItemClick(location: Location)
}