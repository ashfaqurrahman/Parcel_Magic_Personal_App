package com.airposted.bitoronbd.ui.product

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.Navigation
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentProductBinding
import com.airposted.bitoronbd.ui.location_set.LocationSetFragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import java.util.*

class ProductFragment : Fragment(), OnMapReadyCallback, KodeinAware {

    private lateinit var mMap: GoogleMap
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLastLocation: Location
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var productBinding: FragmentProductBinding
    override val kodein by kodein()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        productBinding = FragmentProductBinding.inflate(inflater, container, false)

        return productBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mLocationRequest = LocationRequest()

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapProduct) as SupportMapFragment
        mapFragment.getMapAsync(this)

        productBinding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        productBinding.myLocation.setOnClickListener {
            val cameraPosition =
                CameraPosition.Builder()
                    .target(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                    .zoom(18f).build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

        productBinding.addressLayout.setOnClickListener {
            /*val dialogs = Dialog(requireActivity())
            dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogs.setContentView(R.layout.receiver_address)
            dialogs.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogs.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,  //w
                ViewGroup.LayoutParams.MATCH_PARENT //h
            )

            dialogs.window?.attributes?.windowAnimations = R.style.DialogAnimation_2

            dialogs.findViewById<TextView>(R.id.toolbar_title).text = "Where Are you sending?"
            dialogs.findViewById<ImageView>(R.id.backImage).setOnClickListener {
                dialogs.dismiss()
            }

            dialogs.setCancelable(true)

            dialogs.show()*/

            Navigation.findNavController(requireView()).navigate(
                R.id.receiverAddressFragment
            )
        }

    }

    override fun onPause() {
        super.onPause()

        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                val location = locationList[locationList.size - 1]
                mLastLocation = location

                val latLng = LatLng(location.latitude, location.longitude)
                val points: MutableList<LatLng> = ArrayList()
                points.add(LatLng(23.843974, 90.370339))
                points.add(LatLng(23.749419, 90.354546))
                points.add(LatLng(23.752248, 90.499085))
                points.add(LatLng(23.861558, 90.469903))

                //val polygon: Polygon = mMap.addPolygon(PolygonOptions().addAll(points))
                val contain = PolyUtil.containsLocation(latLng, points, true)

                val cameraPosition =
                    CameraPosition.Builder().target(LatLng(latLng.latitude, latLng.longitude))
                        .zoom(16f).build()
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                mMap.setOnCameraIdleListener {
                    val center = mMap.cameraPosition.target
                    val geo = Geocoder(requireActivity(), Locale.getDefault())
                    val addresses = geo.getFromLocation(center.latitude, center.longitude, 1)
                    if (addresses.isEmpty()) {
                        productBinding.address.text = getString(R.string.searching)
                    } else {
                        var locationString: String
                        locationString = if (addresses[0].featureName == null) {
                            ""
                        } else {
                            addresses[0].featureName
                        }
                        if (addresses[0].thoroughfare == null) {
                            locationString += ""
                        } else {
                            locationString = locationString + ", " + addresses[0].thoroughfare
                        }
                        productBinding.address.text = locationString
                    }
                    productBinding.receiverAddress.visibility = View.VISIBLE
                }

                mMap.setOnCameraMoveStartedListener { reason ->
                    when (reason) {
                        OnCameraMoveStartedListener.REASON_GESTURE -> {
                            productBinding.address.text = getString(R.string.searching)
                            productBinding.receiverAddress.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.isMyLocationEnabled = true

        mMap.uiSettings.isMyLocationButtonEnabled = false

        mLocationRequest.interval = 120000

        mLocationRequest.fastestInterval = 120000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                mFusedLocationClient!!.requestLocationUpdates(
                    mLocationRequest, mLocationCallback,
                    Looper.myLooper()
                )
            }
        } else {
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
            )
        }
    }

}