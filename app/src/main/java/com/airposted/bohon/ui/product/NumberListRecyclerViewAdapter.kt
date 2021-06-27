package com.airposted.bohon.ui.product

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.airposted.bohon.BR
import com.airposted.bohon.R
import com.airposted.bohon.databinding.ContactInfoBinding

class NumberListRecyclerViewAdapter(
    private val dataModelList: ArrayList<String>,
    private val listener: NumberClickListener
) : RecyclerView.Adapter<NumberListRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: ContactInfoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.contact_info, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataModel = dataModelList[position]
        holder.bind(dataModel)

        holder.binding.phoneNumber.setOnClickListener { listener.onItemClick(dataModel) }
    }

    override fun getItemCount(): Int {
        return dataModelList.size
    }

    inner class ViewHolder(var binding: ContactInfoBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(obj: Any?) {
            binding.setVariable(BR.number, obj)
            binding.executePendingBindings()
        }
    }
}