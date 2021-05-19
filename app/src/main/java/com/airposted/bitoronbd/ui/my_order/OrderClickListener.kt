package com.airposted.bitoronbd.ui.my_order

import com.airposted.bitoronbd.model.DataX

interface OrderClickListener {
    fun onItemClick(order: DataX)
}