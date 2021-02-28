package com.airposted.bitoronbd.ui.product

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.airposted.bitoronbd.R
import java.util.*

class DeliveryAdapter(context: Context?, countryList: ArrayList<DeliveryItem>?) :
    ArrayAdapter<DeliveryItem?>(
        context!!, 0, countryList!! as List<DeliveryItem?>
    ) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView!!, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView!!, parent)
    }

    private fun initView(position: Int, convertView: View, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                R.layout.delivery_type_item, parent, false
            )
        }
        val imageViewFlag = convertView.findViewById<ImageView>(R.id.image_view_flag)
        val textViewName = convertView.findViewById<TextView>(R.id.text_view_name)
        val currentItem = getItem(position)
        if (currentItem != null) {
            imageViewFlag.setImageResource(currentItem.flagImage)
            textViewName.text = currentItem.countryName
        }
        return convertView
    }
}