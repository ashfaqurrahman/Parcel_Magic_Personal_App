package com.airposted.bohon.ui.my_order

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.airposted.bohon.BR
import com.airposted.bohon.R
import com.airposted.bohon.databinding.MyParcelListItemBinding
import com.airposted.bohon.model.DataX

class OrderListRecyclerViewAdapter(
    private val dataModelList: List<DataX>,
    private val context: Context,
    private val listener: OrderClickListener
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

        when(dataModel.current_status){
            2 -> {
                holder.binding.imgPending.alpha = 1F
                holder.binding.tvPending.alpha = 1F
                holder.binding.imgAccept.alpha = 0.3F
                holder.binding.tvAccept.alpha = 0.3F
                holder.binding.imgCollected.alpha = 0.3F
                holder.binding.tvCollected.alpha = 0.3F
                holder.binding.imgDelivered.alpha = 0.3F
                holder.binding.tvDelivered.alpha = 0.3F
            }
            3 -> {
                holder.binding.imgPending.alpha = 0.3F
                holder.binding.tvPending.alpha = 0.3F
                holder.binding.imgAccept.alpha = 1F
                holder.binding.tvAccept.alpha = 1F
                holder.binding.imgCollected.alpha = 0.3F
                holder.binding.tvCollected.alpha = 0.3F
                holder.binding.imgDelivered.alpha = 0.3F
                holder.binding.tvDelivered.alpha = 0.3F
            }
            4 -> {
                holder.binding.imgPending.alpha = 0.3F
                holder.binding.tvPending.alpha = 0.3F
                holder.binding.imgAccept.alpha = 0.3F
                holder.binding.tvAccept.alpha = 0.3F
                holder.binding.imgCollected.alpha = 1F
                holder.binding.tvCollected.alpha = 1F
                holder.binding.imgDelivered.alpha = 0.3F
                holder.binding.tvDelivered.alpha = 0.3F
            }
            5 -> {
                holder.binding.imgPending.alpha = 0.3F
                holder.binding.tvPending.alpha = 0.3F
                holder.binding.imgAccept.alpha = 0.3F
                holder.binding.tvAccept.alpha = 0.3F
                holder.binding.imgCollected.alpha = 0.3F
                holder.binding.tvCollected.alpha = 0.3F
                holder.binding.imgDelivered.alpha = 1F
                holder.binding.tvDelivered.alpha = 1F
            }
        }
        holder.binding.viewOrder.setOnClickListener { listener.onItemClick(dataModel) }
    }

    override fun getItemCount(): Int {
        return dataModelList.size
    }

    inner class ViewHolder(var binding: MyParcelListItemBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(obj: Any?) {
            binding.setVariable(BR.order, obj)
            binding.executePendingBindings()
        }
    }
}