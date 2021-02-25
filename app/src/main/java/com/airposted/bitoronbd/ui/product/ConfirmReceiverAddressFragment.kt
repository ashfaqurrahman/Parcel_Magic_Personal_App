package com.airposted.bitoronbd.ui.product

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentConfirmReceiverAddressBinding
import com.airposted.bitoronbd.ui.location_set.LocationSetViewModel
import com.airposted.bitoronbd.ui.location_set.LocationSetViewModelFactory
import com.airposted.bitoronbd.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
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
                        lineoption.color(Color.BLACK)
                        lineoption.geodesic(true)
                    }
                    googleMap.addPolyline(lineoption)
                    for (i in 0 until list.routes[0].legs[0].steps.size){
                        distance += list.routes[0].legs[0].steps[i].distance.value
                    }
                    Log.e("Distance: ", (distance / 1000).toString())
                    googleMap.addMarker(
                        MarkerOptions().position(location1).title(
                            PreferenceProvider(
                                requireActivity()
                            ).getSharedPreferences("location_name")
                        )
                    ).showInfoWindow()

                    googleMap.addMarker(
                        MarkerOptions().position(location2)
                            .title(requireArguments().getString("location_name"))
                    ).showInfoWindow()
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location1, 13f))
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
                    binding.rootLayout.snackbar(
                        ((distance / 1000) * PreferenceProvider(
                            requireActivity()
                        ).getSharedPreferences("rate")!!.toFloat()).toString()
                    )
                }
            }
        }
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