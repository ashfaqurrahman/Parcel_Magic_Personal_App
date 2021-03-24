package com.airposted.bitoronbd.ui.my_parcel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.airposted.bitoronbd.BR
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.MyParcelListItemBinding
import com.airposted.bitoronbd.model.DataX

class OrderListRecyclerViewAdapter(
    private val dataModelList: List<DataX>,
    //private val listener: CustomClickListener
) : RecyclerView.Adapter<OrderListRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: MyParcelListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.my_parcel_list_item, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataModel = dataModelList[position]
        holder.bind(dataModel)
        //holder.itemRowBinding.title.setOnClickListener { listener.onItemClick(dataModel) }
    }

    override fun getItemCount(): Int {
        return dataModelList.size
    }

    inner class ViewHolder(var itemRowBinding: MyParcelListItemBinding) : RecyclerView.ViewHolder(
        itemRowBinding.root
    ) {
        fun bind(obj: Any?) {
            itemRowBinding.setVariable(BR.order, obj)
            itemRowBinding.executePendingBindings()
        }
    }
}