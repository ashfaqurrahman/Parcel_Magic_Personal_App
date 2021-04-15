package com.airposted.bitoronbd.ui.home

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.BuildConfig
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentHomeBinding
import com.airposted.bitoronbd.model.LocationDetails
import com.airposted.bitoronbd.ui.WebViewFragment
import com.airposted.bitoronbd.ui.auth.SignInSignUpActivity
import com.airposted.bitoronbd.ui.history.MyParcelHistoryFragment
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.ui.more.MoreFragment
import com.airposted.bitoronbd.ui.my_parcel.MyParcelFragment
import com.airposted.bitoronbd.ui.product.PackageGuidelineFragment
import com.airposted.bitoronbd.ui.product.ParcelTypeFragment
import com.airposted.bitoronbd.utils.*
import com.airposted.bitoronbd.utils.ApiException
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


open class HomeFragment : Fragment(R.layout.fragment_home),
    NavigationView.OnNavigationItemSelectedListener, KodeinAware, OnMapReadyCallback {

    private lateinit var homeBinding: FragmentHomeBinding
    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()
    private lateinit var viewModel: HomeViewModel
    var myCommunicator: CommunicatorFragmentInterface? = null
    private lateinit var mMap: GoogleMap

    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000

    private val REQUEST_CHECK_SETTINGS = 0x1
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var locationDetailsImp: LocationDetails

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)

        return homeBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), factory).get(HomeViewModel::class.java)
        bindUI()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun bindUI() = Coroutines.main {

        setProgressDialog(requireActivity())

        googleApiClient = getAPIClientInstance()
        googleApiClient.connect()

        viewModel.gps.observe(viewLifecycleOwner, {
            if (it) {
                homeBinding.titleLayout.visibility = View.VISIBLE
                homeBinding.title2Layout.visibility = View.GONE

            } else {
                homeBinding.titleLayout.visibility = View.GONE
                homeBinding.title2Layout.visibility = View.VISIBLE
            }
        })

        homeBinding.shareLocation.setOnClickListener {

            requestGPSSettings()
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.current_location) as SupportMapFragment
        mapFragment.getMapAsync(this)

        homeBinding.menu.setOnClickListener {
            homeBinding.drawerLayout.openDrawer(Gravity.LEFT)
        }

        homeBinding.navigationView.setNavigationItemSelectedListener(this)
        homeBinding.versionName.text = "Version " + BuildConfig.VERSION_NAME
        myCommunicator = context as CommunicatorFragmentInterface

        val hView: View = homeBinding.navigationView.getHeaderView(0)
        val pic = hView.findViewById<CircleImageView>(R.id.profile_image)
        val name = hView.findViewById<TextView>(R.id.user_name)

        viewModel.getName.await().observe(requireActivity(), {
            name.text = it
        })

        Glide.with(requireActivity()).load(
            PersistentUser.getInstance().getUserImage(requireActivity())
        ).placeholder(R.mipmap.ic_launcher).error(
            R.drawable.sample_pro_pic
        ).into(pic)

        homeBinding.expressBtn.setOnClickListener{
            val fragment = ParcelTypeFragment()
            val bundle = Bundle()
            bundle.putString("delivery_type", "express")
            fragment.arguments = bundle
            myCommunicator?.addContentFragment(fragment, true)
        }

        homeBinding.quickBtn.setOnClickListener{
            val fragment = ParcelTypeFragment()
            val bundle = Bundle()
            bundle.putString("delivery_type", "quick")
            fragment.arguments = bundle
            myCommunicator?.addContentFragment(fragment, true)
        }

        homeBinding.whatToSend.setOnClickListener {
            myCommunicator?.addContentFragment(PackageGuidelineFragment(), true)
        }

        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                ContextCompat.getColor(requireActivity(), R.color.color1),
                ContextCompat.getColor(requireActivity(), R.color.color2),
                ContextCompat.getColor(requireActivity(), R.color.color3),
                ContextCompat.getColor(requireActivity(), R.color.color4)
            )
        )

        homeBinding.expressBtn.background = gradientDrawable
        homeBinding.quickBtn.background = gradientDrawable

        lifecycleScope.launch {
            try {
                val settingResponse = viewModel.getSetting()
                PreferenceProvider(requireActivity()).saveSharedPreferences(
                    "rate_quick",
                    settingResponse.rate.perKmPrice.toString()
                )
                PreferenceProvider(requireActivity()).saveSharedPreferences(
                    "rate_express",
                    settingResponse.rate.perKmPrice.toString()
                )
                PreferenceProvider(requireActivity()).saveSharedPreferences(
                    "base_price_quick",
                    settingResponse.rate.basePriceQuick.toString()
                )
                PreferenceProvider(requireActivity()).saveSharedPreferences(
                    "base_price_express",
                    settingResponse.rate.basePriceExpress.toString()
                )
                dismissDialog()
            } catch (e: ApiException) {
                dismissDialog()
                homeBinding.rootLayout.snackbar(e.message!!)
                e.printStackTrace()
            } catch (e: NoInternetException) {
                dismissDialog()
                homeBinding.rootLayout.snackbar(e.message!!)
                e.printStackTrace()
            }
        }
    }

    private fun getAPIClientInstance(): GoogleApiClient {
        return GoogleApiClient.Builder(requireActivity())
            .addApi(LocationServices.API).build()
    }

    private fun requestGPSSettings() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    Log.i("", "All location settings are satisfied.")
                    Toast.makeText(
                        requireActivity(),
                        "GPS is already enable",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(
                        "",
                        "Location settings are not satisfied. Show the user a dialog to" + "upgrade location settings "
                    )
                    try {
                        status.startResolutionForResult(
                            requireActivity(),
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (e: SendIntentException) {
                        Log.e("Applicationsett", e.toString())
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    Log.i(
                        "",
                        "Location settings are inadequate, and cannot be fixed here. Dialog " + "not created."
                    )
                    Toast.makeText(
                        requireActivity(),
                        "Location settings are inadequate, and cannot be fixed here",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun openWhatToSendDialog() {
        val orderDialog = Dialog(requireContext())
        orderDialog.setContentView(R.layout.what_to_send_dialog)
        orderDialog.findViewById<TextView>(R.id.got_it).setOnClickListener {
            orderDialog.dismiss()
        }
        orderDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        orderDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        orderDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation_2
        orderDialog.window?.attributes?.gravity = Gravity.BOTTOM
        orderDialog.setCancelable(true)
        orderDialog.show()
    }

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
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isZoomGesturesEnabled = false
        mMap.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = false
        mMap.uiSettings.isScrollGesturesEnabled = false
        mMap.uiSettings.isRotateGesturesEnabled = false
        mMap.mapType = 1

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(), R.raw.style
                )
            )
            if (!success) {
                Log.e("TAG", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("TAG", "Can't find style. Error: ", e)
        }

        viewModel.gps.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.currentLocation.observe(viewLifecycleOwner, { locationDetails ->
                    locationDetailsImp = locationDetails
                    Toast.makeText(
                        requireActivity(),
                        locationDetails.latitude.toString() + "/" + locationDetails.longitude.toString(),
                        Toast.LENGTH_LONG
                    ).show()

                    val cameraPosition =
                        CameraPosition.Builder()
                            .target(
                                LatLng(
                                    locationDetailsImp.latitude,
                                    locationDetailsImp.longitude
                                )
                            )
                            .zoom(15.2f)                   // Sets the zoom
                            .build()
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                })

            } else {
                if (PreferenceProvider(requireActivity()).getSharedPreferences("latitude") != null){
                    val cameraPosition =
                        CameraPosition.Builder()
                            .target(
                                LatLng(
                                    PreferenceProvider(requireActivity()).getSharedPreferences("latitude")!!.toDouble(),
                                    PreferenceProvider(requireActivity()).getSharedPreferences("longitude")!!.toDouble()
                                )
                            )
                            .zoom(15.2f)                   // Sets the zoom
                            .build()
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.my_parcel -> {
                myCommunicator?.addContentFragment(MyParcelFragment(), true)
                homeBinding.drawerLayout.closeDrawers()
            }
            R.id.parcel_history -> {
                myCommunicator?.addContentFragment(MyParcelHistoryFragment(), true)
                homeBinding.drawerLayout.closeDrawers()
            }
            R.id.help -> {

                homeBinding.drawerLayout.closeDrawers()
            }
            R.id.settings -> {
                myCommunicator?.addContentFragment(MoreFragment(), true)
                homeBinding.drawerLayout.closeDrawers()
            }
            R.id.terms_condition -> {
                homeBinding.drawerLayout.closeDrawers()
                val fragment = WebViewFragment()
                val bundle = Bundle()
                bundle.putString("url", "https://airposted.com/page/terms-of-service#:~:text=Airposted%20Referral%20Program%20Terms%20and%20Conditions%2A%20Airposted%20Referral,it%20as%20a%20traveler%20or%20shopper%20or%20buyer.")
                fragment.arguments = bundle
                myCommunicator?.addContentFragment(fragment, true)
            }
            R.id.sign_out -> {
                homeBinding.drawerLayout.closeDrawers()
                PersistentUser.getInstance().logOut(context)
                val intent = Intent(requireContext(), SignInSignUpActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }

        return true
    }
}