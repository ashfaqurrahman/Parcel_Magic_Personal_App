package com.airposted.bohon.ui.history

import com.airposted.bohon.model.DataX

interface OrderHistoryClickListener {
    fun onItemClick(order: DataX)
    fun onRatingClick(order: DataX)
}