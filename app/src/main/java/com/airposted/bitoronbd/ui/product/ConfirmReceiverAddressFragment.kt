package com.airposted.bitoronbd.ui.product

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentConfirmReceiverAddressBinding
import com.airposted.bitoronbd.ui.location_set.LocationSetViewModel
import com.airposted.bitoronbd.ui.location_set.LocationSetViewModelFactory
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class ConfirmReceiverAddressFragment : Fragment(), KodeinAware {

    lateinit var mapFragment : SupportMapFragment
    lateinit var googleMap: GoogleMap
    private lateinit var binding: FragmentConfirmReceiverAddressBinding
    override val kodein by kodein()
    private val factory: LocationSetViewModelFactory by instance()
    private lateinit var viewModel: LocationSetViewModel
    var distance =  0F
    var myCommunicator: CommunicatorFragmentInterface? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConfirmReceiverAddressBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, factory).get(LocationSetViewModel::class.java)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {

        setProgressDialog(requireActivity())
        myCommunicator = context as CommunicatorFragmentInterface

        mapFragment = childFragmentManager.findFragmentById(R.id.mapReceiverDetails) as SupportMapFragment
        mapFragment.getMapAsync {
            googleMap = it
            googleMap.uiSettings.isZoomControlsEnabled = false
            googleMap.uiSettings.isZoomGesturesEnabled = false
            googleMap.uiSettings.isRotateGesturesEnabled = false
            googleMap.uiSettings.isScrollGesturesEnabled = true

            googleMap.setOnMarkerClickListener {
                false
            }
//            googleMap.isMyLocationEnabled = true
            val location1 = LatLng(
                PreferenceProvider(requireActivity()).getSharedPreferences("latitude")!!.toDouble(),
                PreferenceProvider(requireActivity()).getSharedPreferences("longitude")!!.toDouble()
            )

            val location2 = LatLng(
                requireArguments().getDouble("latitude"),
                requireArguments().getDouble("longitude")
            )

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
                    for (i in 0 until list.routes[0].legs[0].steps.size){
                        distance += list.routes[0].legs[0].steps[i].distance.value
                    }

                    val circleDrawable = resources.getDrawable(R.drawable.root_start_point)
                    val markerIcon = getMarkerIconFromDrawable(circleDrawable)
                    googleMap.addMarker(
                        MarkerOptions().position(location1)
                            .icon(markerIcon)
                    ).showInfoWindow()

                    googleMap.setInfoWindowAdapter(object : InfoWindowAdapter {
                        override fun getInfoWindow(marker: Marker?): View? {
                            return null
                        }

                        override fun getInfoContents(marker: Marker): View {
                            val v: View = layoutInflater.inflate(R.layout.row, null)
                            val info1 = v.findViewById(R.id.text_view_name) as TextView
                            info1.text = "Fecha: "
                            googleMap.setOnInfoWindowClickListener {
                                val fragmento: Fragment
                                /*fragmento = HistorialFragment()
                                val bundle = Bundle()
                                bundle.putString("title", info1.text.toString())
                                fragmento.arguments = bundle
                                getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.content_principal, fragmento)
                                    .commit()*/
                            }
                            return v
                        }
                    })

                    val circleDrawable1 = resources.getDrawable(R.drawable.ic_marker_without_space)
                    val markerIcon1 = getMarkerIconFromDrawable(circleDrawable1)
                    googleMap.addMarker(
                        MarkerOptions().position(location2)
                            .icon(markerIcon1)
                    ).showInfoWindow()
                    if (distance/1000 > 5){
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location1, 12f))
                    } else {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location1, 13f))
                    }
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

        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.confirmReceiverAddress.setOnClickListener {
            when {
                binding.receiverName.text.isNullOrEmpty() -> {
                    binding.rootLayout.snackbar("Please enter receiver name")
                }
                binding.receiverPhone.text.isNullOrEmpty() -> {
                    binding.rootLayout.snackbar("Please enter receiver phone number")
                }
                else -> {
                    val fragment = ConfirmDeliveryRequestFragment()
                    val bundle = Bundle()
                    bundle.putString("name", binding.receiverName.text.toString())
                    bundle.putString("phone", binding.receiverPhone.text.toString())
                    bundle.putString("info", binding.receiverDirection.text.toString())
                    bundle.putFloat("distance", distance / 1000)
                    bundle.putString("location_name", requireArguments().getString("location_name"))
                    bundle.putDouble("latitude", requireArguments().getDouble("latitude"))
                    bundle.putDouble("longitude", requireArguments().getDouble("longitude"))
                    bundle.putString("city", requireArguments().getString("city"))
                    bundle.putString("area", requireArguments().getString("area"))
                    fragment.arguments = bundle
                    myCommunicator?.addContentFragment(fragment, true)
                }
            }
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
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&key=AIzaSyB8gzYgvsy-1TufBYLYaD58EYDTWUZBWZQ&sensor=false&mode=driving"
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