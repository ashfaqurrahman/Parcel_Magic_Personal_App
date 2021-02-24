package com.airposted.bitoronbd.ui.product

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentProductBinding
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.ui.main.MainActivity
import com.airposted.bitoronbd.utils.snackbar
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import java.util.*

class ProductFragment : Fragment(), OnMapReadyCallback, KodeinAware, LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var productBinding: FragmentProductBinding
    override val kodein by kodein()
    var myCommunicator: CommunicatorFragmentInterface? = null
    private lateinit var builder: LocationSettingsRequest.Builder
    private lateinit var locationManager: LocationManager

    private var latitude = ""
    private var longitude = ""

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
        myCommunicator = context as CommunicatorFragmentInterface

        productBinding.receiverAddress.isEnabled = false

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapProduct) as SupportMapFragment
        mapFragment.getMapAsync(this)

        productBinding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        productBinding.myLocation.setOnClickListener {
            Permissions.check(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION,
                null,
                object : PermissionHandler() {
                    override fun onGranted() {

                        val manager: LocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                            getLocation()
                        } else {
                            val request = LocationRequest()
                                .setFastestInterval(0)
                                .setInterval(0)
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

                            builder = LocationSettingsRequest.Builder()
                                .addLocationRequest(request)

                            val result = LocationServices.getSettingsClient(requireActivity())
                                .checkLocationSettings(
                                    builder.build()
                                )

                            result.addOnCompleteListener {
                                try {
                                    it.getResult(com.google.android.gms.common.api.ApiException::class.java)
                                } catch (e: com.google.android.gms.common.api.ApiException) {
                                    when (e.statusCode) {
                                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                                            try {
                                                val resolvableApiException =
                                                    e as ResolvableApiException
                                                resolvableApiException.startResolutionForResult(
                                                    requireActivity(),
                                                    REQUEST_CHANGE_CODE
                                                )
                                            } catch (ex: ClassCastException) {

                                            }
                                        }
                                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                            val intent = Intent().apply {
                                                action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                                            }
                                            startActivityForResult(
                                                intent,
                                                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
                                            )
                                        }
                                    }
                                }
                            }
                            getLocation()
                        }
                    }

                    override fun onDenied(
                        context: Context?,
                        deniedPermissions: ArrayList<String>?
                    ) {
                        super.onDenied(context, deniedPermissions)
                    }

                    override fun onBlocked(
                        context: Context?,
                        blockedList: ArrayList<String>?
                    ): Boolean {
                        return super.onBlocked(context, blockedList)
                    }
                })
            /*val cameraPosition =
                CameraPosition.Builder()
                    .target(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                    .zoom(18f).build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))*/
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


            myCommunicator?.addContentFragment(ReceiverAddressFragment(), true)

            //findNavController().navigate(R.id.action_productFragment_to_receiverAddressFragment)
        }

        productBinding.receiverAddress.setOnClickListener {
            val latLng = LatLng(latitude.toDouble(), longitude.toDouble())
            val points: MutableList<LatLng> = ArrayList()
            points.add(LatLng(23.843974, 90.370339))
            points.add(LatLng(23.749419, 90.354546))
            points.add(LatLng(23.752248, 90.499085))
            points.add(LatLng(23.861558, 90.469903))

            //val polygon: Polygon = mMap.addPolygon(PolygonOptions().addAll(points))
            val contain = PolyUtil.containsLocation(latLng, points, true)

            if (contain) {

            } else {
                productBinding.rootLayout.snackbar("Sorry!! We are currently not providing our service to this area")
            }
        }

    }

    override fun onLocationChanged(location: Location) {
        try {
            val cameraPosition =
                CameraPosition.Builder()
                    .target(LatLng(location.latitude, location.longitude))
                    .zoom(16f).build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        try {
            locationManager =
                requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                5f,
                this
            )
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                5f,
                this
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        mMap.isMyLocationEnabled = true

        mMap.uiSettings.isMyLocationButtonEnabled = false

        mMap.setOnCameraIdleListener {
            val center = mMap.cameraPosition.target
            val geo = Geocoder(requireActivity(), Locale.getDefault())
            val addresses = geo.getFromLocation(center.latitude, center.longitude, 1)
            if (addresses.isEmpty()) {
                //productBinding.address.text = getString(R.string.searching)
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
                latitude = center.latitude.toString()
                longitude = center.longitude.toString()
                productBinding.address.text = locationString
                productBinding.receiverAddress.background = ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.after_button_bg
                )
                productBinding.receiverAddress.isEnabled = true
            }
            productBinding.receiverAddress.visibility = View.VISIBLE
        }

        mMap.setOnCameraMoveStartedListener { reason ->
            when (reason) {
                OnCameraMoveStartedListener.REASON_GESTURE -> {
                    productBinding.address.text = getString(R.string.searching)
                    productBinding.receiverAddress.visibility = View.GONE
                    productBinding.receiverAddress.background = ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.before_button_bg
                    )
                    productBinding.receiverAddress.isEnabled = false
                }
            }
        }
    }

    companion object {
        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_CHANGE_CODE = 35
    }

}