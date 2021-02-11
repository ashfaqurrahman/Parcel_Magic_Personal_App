package com.airposted.bitoronbd.ui.location_set

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.airposted.bitoronbd.BR
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.ItemLocationSearchBinding
import com.airposted.bitoronbd.model.Prediction

class MyRecyclerViewAdapter(
    private val dataModelList: List<Prediction>,
    private val listener: CustomClickListener
) : RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>() {
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