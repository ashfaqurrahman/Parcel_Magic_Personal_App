package com.airposted.bohon.ui.my_order

import com.airposted.bohon.model.DataX

interface OrderClickListener {
    fun onItemClick(order: DataX)
}