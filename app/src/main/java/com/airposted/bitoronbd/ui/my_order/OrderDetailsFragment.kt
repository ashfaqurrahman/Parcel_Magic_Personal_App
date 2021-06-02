package com.airposted.bitoronbd.ui.my_order

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.util.MalformedJsonException
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentOrderDetailsBinding
import com.airposted.bitoronbd.ui.home.HomeFragment
import com.airposted.bitoronbd.ui.location_set.LocationSetViewModel
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class OrderDetailsFragment : Fragment(), KodeinAware {
    lateinit var mapFragment : SupportMapFragment
    lateinit var googleMap: GoogleMap
    override val kodein by kodein()
    private val factory: MyParcelViewModelFactory by instance()
    private lateinit var binding: FragmentOrderDetailsBinding
    private lateinit var viewModel: MyParcelViewModel
    var communicatorFragmentInterface: CommunicatorFragmentInterface? = null
    var distance =  0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, factory).get(MyParcelViewModel::class.java)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        communicatorFragmentInterface = context as CommunicatorFragmentInterface
        binding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.toolbar.toolbarTitle.text = getString(R.string.order_detaisl)

        binding.deliveryType.text = requireArguments().getString("personal_order_type").toString() + " Delivery"
        binding.invoice.text = "#" + requireArguments().getString("invoice")
        binding.quantity.text = "0" + requireArguments().getString("item_qty")
        binding.distance.text = requireArguments().getInt("distance").toString() + " km"
        binding.charge.text = "BDT " + requireArguments().getInt("delivery_charge").toString()
        binding.deliveryDate.text = requireArguments().getString("order_date")
        binding.receiverName.text = requireArguments().getString("recp_name")
        binding.receiverPhone.text = requireArguments().getString("recp_phone")
        binding.senderName.text = PersistentUser.getInstance().getFullName(requireContext())
        binding.senderPhone.text = PersistentUser.getInstance().getPhoneNumber(requireContext())
        binding.from.text = requireArguments().getString("pick_address")
        binding.to.text = requireArguments().getString("recp_address")

        mapFragment = childFragmentManager.findFragmentById(R.id.mapReceiverDetails) as SupportMapFragment
        mapFragment.getMapAsync {
            googleMap = it
            googleMap.uiSettings.isZoomControlsEnabled = false
            googleMap.uiSettings.isZoomGesturesEnabled = false
            googleMap.uiSettings.isRotateGesturesEnabled = false
            googleMap.uiSettings.isScrollGesturesEnabled = false

            googleMap.setOnMarkerClickListener {
                false
            }
//            googleMap.isMyLocationEnabled = true
            val location1 = LatLng(
                requireArguments().getDouble("sender_latitude"),
                requireArguments().getDouble("sender_longitude")
            )

            val location2 = LatLng(
                requireArguments().getDouble("receiver_latitude"),
                requireArguments().getDouble("receiver_longitude")
            )

            val bounds = LatLngBounds.Builder()
            bounds.include(location1)
            bounds.include(location2)

            lifecycleScope.launch {
                try {
                    val url = getDirectionURL(location1, location2)
                    val list = viewModel.getDirections(url)
                    val result =  ArrayList<List<LatLng>>()
                    val path =  ArrayList<LatLng>()
                    for (i in 0 until list.routes[0].legs[0].steps.size){
                        path.addAll(decodePolyline(list.routes[0].legs[0].steps[i].polyline.points))
                    }
                    result.add(path)
                    val lineoption = PolylineOptions()
                    for (i in result.indices){
                        lineoption.addAll(result[i])
                        lineoption.width(6f)
                        lineoption.color(resources.getColor(R.color.blue))
                        lineoption.geodesic(true)
                    }
                    googleMap.addPolyline(lineoption)
                    /*for (i in 0 until list.routes[0].legs[0].steps.size){
                        distance += list.routes[0].legs[0].steps[i].distance.value
                    }*/

                    distance = list.routes[0].legs[0].distance.value

                    /*val circleDrawable = resources.getDrawable(R.drawable.root_start_point)
                    val markerIcon = getMarkerIconFromDrawable(circleDrawable)
                    googleMap.addMarker(
                        MarkerOptions().position(location1)
                            .icon(markerIcon)
                    ).showInfoWindow()

                    googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                        override fun getInfoWindow(marker: Marker?): View? {
                            return null
                        }

                        override fun getInfoContents(marker: Marker): View {
                            val v: View = layoutInflater.inflate(R.layout.row, null)
                            val info1 = v.findViewById(R.id.text_view_name) as TextView
                            info1.text = requireArguments().getString("recp_address")
                            googleMap.setOnInfoWindowClickListener {
                                val fragmento: Fragment
                                *//*fragmento = HistorialFragment()
                                val bundle = Bundle()
                                bundle.putString("title", info1.text.toString())
                                fragmento.arguments = bundle
                                getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.content_principal, fragmento)
                                    .commit()*//*
                            }
                            return v
                        }
                    })

                    val circleDrawable1 = resources.getDrawable(R.drawable.ic_marker)
                    val markerIcon1 = getMarkerIconFromDrawable(circleDrawable1)
                    googleMap.addMarker(
                        MarkerOptions().position(location2)
                            .icon(markerIcon1)
                    ).showInfoWindow()*/

                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            bounds.build(),
                            mapFragment.requireView().width,
                            mapFragment.requireView().height,
                            (mapFragment.requireView().height * 0.05f).toInt()
                        )
                    )

                    /*if (distance/1000 > 5){
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location1, 10f))
                    } else {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location1, 13f))
                    }*/
                    dismissDialog()
                } catch (e: ApiException) {
                    dismissDialog()
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    dismissDialog()
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                }
            }

        }

        when (requireArguments().getInt("current_status")) {
            2 -> {
                binding.status.text = "Pending"
                binding.cancelLayout.visibility = View.VISIBLE
            }
            3 -> {
                binding.status.text = "Accepted"
            }
            4 -> {
                binding.status.text = "Collected"
            }
            5 -> {
                binding.status.text = "Delivered"
            }
            13 -> {
                binding.status.text = "Cancelled"
            }
        }

        when(requireArguments().getInt("item_type")) {
            1 -> {
                binding.size.text = getString(R.string.envelope_size1)
                binding.icon.setBackgroundResource(R.drawable.ic_document_large_icon)
            }
            2 -> {
                binding.size.text = getString(R.string.small_size1)
                binding.icon.setBackgroundResource(R.drawable.ic_box_large_icon)
            }
            3 -> {
                binding.size.text = getString(R.string.large_size1)
                binding.icon.setBackgroundResource(R.drawable.ic_box_large_icon)
            }
        }

        binding.cancelOrder.setOnClickListener {
            val dialogs = Dialog(requireActivity())
            dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogs.setContentView(R.layout.cancel_order_dialog)
            dialogs.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogs.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,  //w
                ViewGroup.LayoutParams.MATCH_PARENT //h
            )

            val cancel = dialogs.findViewById<TextView>(R.id.cancel)
            val ok = dialogs.findViewById<TextView>(R.id.ok)
            cancel.setOnClickListener {
                dialogs.dismiss()
            }

            ok.setOnClickListener {
                dialogs.dismiss()
                setProgressDialog(requireActivity())
                lifecycleScope.launch {
                    try {
                        val response = viewModel.changeStatus(requireArguments().getString("invoice")!!, 13)
                        if (response.success) {
                            dismissDialog()
                            Toast.makeText(
                                requireContext(),
                                "Cancelled Order Successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            communicatorFragmentInterface!!.addContentFragment(HomeFragment(), true)
                        }
                        else {
                            binding.rootLayout.snackbar(response.msg)
                            dismissDialog()
                        }
                    } catch (e: MalformedJsonException) {
                        dismissDialog()
                        binding.rootLayout.snackbar(e.message!!)
                        e.printStackTrace()
                    } catch (e: ApiException) {
                        dismissDialog()
                        binding.rootLayout.snackbar(e.message!!)
                        e.printStackTrace()
                    } catch (e: NoInternetException) {
                        dismissDialog()
                        binding.rootLayout.snackbar(e.message!!)
                        e.printStackTrace()
                    }

                }
            }

            dialogs.setCancelable(true)
            dialogs.show()
        }
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor? {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun getDirectionURL(origin: LatLng, dest: LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&key=AIzaSyAJnceVASls_tIv4MiZFkzY1ZrVgu6GmW4&sensor=false&mode=driving"
    }

    private fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }
}