package com.airposted.bitoronbd.ui.my_parcel

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.airposted.bitoronbd.BR
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.MyParcelListItemBinding
import com.airposted.bitoronbd.model.DataX
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class OrderListRecyclerViewAdapter(
    private val dataModelList: List<DataX>,
    private val context: Context
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

        /*val mapView = holder.itemRowBinding.mapView
        //mapView.onResume() // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(context.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mapView.getMapAsync { mMap ->

            val currentLatLng = LatLng(23.786474, 90.403455)
            mMap!!.addMarker(MarkerOptions().position(currentLatLng).title("Test"))


            // For zooming automatically to the location of the marker
            val cameraPosition = CameraPosition.Builder().target(currentLatLng).zoom(14f).build()
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }*/

        when(dataModel.current_status){
            2 -> {
                holder.itemRowBinding.imgPending.alpha = 1F
                holder.itemRowBinding.tvPending.alpha = 1F
                holder.itemRowBinding.imgAccept.alpha = 0.3F
                holder.itemRowBinding.tvAccept.alpha = 0.3F
                holder.itemRowBinding.imgCollected.alpha = 0.3F
                holder.itemRowBinding.tvCollected.alpha = 0.3F
                holder.itemRowBinding.imgDelivered.alpha = 0.3F
                holder.itemRowBinding.tvDelivered.alpha = 0.3F
            }
            3 -> {
                holder.itemRowBinding.imgPending.alpha = 0.3F
                holder.itemRowBinding.tvPending.alpha = 0.3F
                holder.itemRowBinding.imgAccept.alpha = 1F
                holder.itemRowBinding.tvAccept.alpha = 1F
                holder.itemRowBinding.imgCollected.alpha = 0.3F
                holder.itemRowBinding.tvCollected.alpha = 0.3F
                holder.itemRowBinding.imgDelivered.alpha = 0.3F
                holder.itemRowBinding.tvDelivered.alpha = 0.3F
            }
            4 -> {
                holder.itemRowBinding.imgPending.alpha = 0.3F
                holder.itemRowBinding.tvPending.alpha = 0.3F
                holder.itemRowBinding.imgAccept.alpha = 0.3F
                holder.itemRowBinding.tvAccept.alpha = 0.3F
                holder.itemRowBinding.imgCollected.alpha = 1F
                holder.itemRowBinding.tvCollected.alpha = 1F
                holder.itemRowBinding.imgDelivered.alpha = 0.3F
                holder.itemRowBinding.tvDelivered.alpha = 0.3F
            }
            5 -> {
                holder.itemRowBinding.imgPending.alpha = 0.3F
                holder.itemRowBinding.tvPending.alpha = 0.3F
                holder.itemRowBinding.imgAccept.alpha = 0.3F
                holder.itemRowBinding.tvAccept.alpha = 0.3F
                holder.itemRowBinding.imgCollected.alpha = 0.3F
                holder.itemRowBinding.tvCollected.alpha = 0.3F
                holder.itemRowBinding.imgDelivered.alpha = 1F
                holder.itemRowBinding.tvDelivered.alpha = 1F
            }
        }
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