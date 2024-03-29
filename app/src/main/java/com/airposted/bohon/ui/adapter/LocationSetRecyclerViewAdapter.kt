package com.airposted.bohon.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.airposted.bohon.BR
import com.airposted.bohon.R
import com.airposted.bohon.data.db.Location
import com.airposted.bohon.databinding.ItemLocationSearchBinding
import com.airposted.bohon.ui.location_set.CustomClickListener

class LocationSetRecyclerViewAdapter(
    private val dataModelList: List<Location>,
    private val listener: CustomClickListener
) : RecyclerView.Adapter<LocationSetRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: ItemLocationSearchBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_location_search, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataModel = dataModelList[position]
        holder.bind(dataModel)
        holder.itemRowBinding.title.setOnClickListener { listener.onItemClick(dataModel) }
    }

    override fun getItemCount(): Int {
        return dataModelList.size
    }

    inner class ViewHolder(var itemRowBinding: ItemLocationSearchBinding) : RecyclerView.ViewHolder(
        itemRowBinding.root
    ) {
        fun bind(obj: Any?) {
            itemRowBinding.setVariable(BR.location, obj)
            itemRowBinding.executePendingBindings()
        }
    }
}