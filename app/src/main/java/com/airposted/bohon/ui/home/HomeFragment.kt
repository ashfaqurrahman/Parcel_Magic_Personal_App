package com.airposted.bohon.ui.home

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bohon.BuildConfig
import com.airposted.bohon.R
import com.airposted.bohon.data.network.preferences.PreferenceProvider
import com.airposted.bohon.databinding.FragmentHomeBinding
import com.airposted.bohon.model.LocationDetailsWithName
import com.airposted.bohon.ui.auth.AuthActivity
import com.airposted.bohon.ui.create_parcel.PackageGuidelineFragment
import com.airposted.bohon.ui.create_parcel.ParcelTypeFragment
import com.airposted.bohon.ui.help.HelpFragment
import com.airposted.bohon.ui.history.MyParcelHistoryFragment
import com.airposted.bohon.ui.main.CommunicatorFragmentInterface
import com.airposted.bohon.ui.my_order.CancelOrderFragment
import com.airposted.bohon.ui.my_order.MyParcelFragment
import com.airposted.bohon.ui.profile.ProfileFragment
import com.airposted.bohon.ui.termsconditions.TermsConditionsFragment
import com.airposted.bohon.utils.*
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.stream.MalformedJsonException
import com.judemanutd.autostarter.AutoStartPermissionHelper
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


open class HomeFragment : Fragment(R.layout.fragment_home),
    NavigationView.OnNavigationItemSelectedListener, KodeinAware, OnMapReadyCallback {

    private lateinit var binding: FragmentHomeBinding
    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()
    private lateinit var viewModel: HomeViewModel
    var myCommunicator: CommunicatorFragmentInterface? = null
    private lateinit var mMap: GoogleMap
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000
    private val REQUEST_CHECK_SETTINGS = 0x1
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var locationDetailsImp: LocationDetailsWithName
    private lateinit var dialogs: Dialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), factory).get(HomeViewModel::class.java)
        bindUI()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun bindUI() = Coroutines.main {

        if (AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(requireActivity())) {
            AutoStartPermissionHelper.getInstance().getAutoStartPermission(requireActivity())
        } else {
            Log.e("aaaaaa", "Autostart Disabled")
            //requireActivity().startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
            if (NotificationManagerCompat.from(requireActivity()).areNotificationsEnabled()) {
                Log.e("aaaaaa", "Enabled")
            } else {
                Log.e("aaaaaa", "Disabled")
                openAppNotificationSettings(requireActivity())
            }
        }

        setProgressDialog(requireActivity())

        googleApiClient = getAPIClientInstance()
        googleApiClient.connect()

        viewModel.gps.observe(viewLifecycleOwner, {
            if (it) {
                binding.titleLayout.visibility = View.VISIBLE
                binding.title2Layout.visibility = View.GONE

            } else {
                binding.titleLayout.visibility = View.GONE
                binding.title2Layout.visibility = View.VISIBLE
            }
        })

        binding.shareLocation.setOnClickListener {
            requestGPSSettings()
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.current_location) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.menu.setOnClickListener {
            binding.drawerLayout.openDrawer(Gravity.LEFT)
        }

        binding.navigationView.setNavigationItemSelectedListener(this)
        binding.versionName.text = "Version " + BuildConfig.VERSION_NAME
        myCommunicator = context as CommunicatorFragmentInterface

        val hView: View = binding.navigationView.getHeaderView(0)
        val pic = hView.findViewById<CircleImageView>(R.id.profile_image)
        val name = hView.findViewById<TextView>(R.id.user_name)

        viewModel.name.observe(viewLifecycleOwner) {
            name.text = it
        }

        viewModel.image.observe(viewLifecycleOwner) {
            Glide.with(requireActivity()).load(
                it
            ).placeholder(R.mipmap.ic_launcher).error(
                R.drawable.sample_pro_pic
            ).into(pic)
        }

        binding.expressBtn.setOnClickListener{
            val fragment = ParcelTypeFragment()
            val bundle = Bundle()
            bundle.putInt("delivery_type", 2)
            fragment.arguments = bundle
            myCommunicator?.addContentFragment(fragment, true)
        }

        binding.quickBtn.setOnClickListener{
            val fragment = ParcelTypeFragment()
            val bundle = Bundle()
            bundle.putInt("delivery_type", 1)
            fragment.arguments = bundle
            myCommunicator?.addContentFragment(fragment, true)
        }

        binding.whatToSend.setOnClickListener {
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

//        binding.expressBtn.background = gradientDrawable
//        binding.quickBtn.background = gradientDrawable

        lifecycleScope.launch {
            try {
                val settingResponse = viewModel.getSetting()
                PreferenceProvider(requireActivity()).saveSharedPreferences(
                    "per_km_price_quick",
                    settingResponse.rate.perKmPriceQuick.toString()
                )
                PreferenceProvider(requireActivity()).saveSharedPreferences(
                    "per_km_price_express",
                    settingResponse.rate.perKmPriceExpress.toString()
                )
                PreferenceProvider(requireActivity()).saveSharedPreferences(
                    "base_price_quick",
                    settingResponse.rate.basePriceQuick.toString()
                )
                PreferenceProvider(requireActivity()).saveSharedPreferences(
                    "base_price_express",
                    settingResponse.rate.basePriceExpress.toString()
                )

                lifecycleScope.launch {
                    try {
                        val response = viewModel.getUserBasedCoupons()
                        if (response.coupons.isNotEmpty()) {
                            binding.coupon.text = response.coupons[0].coupon_text
                            binding.couponTitle.text = response.coupons[0].coupon_title
                            PreferenceProvider(requireActivity()).saveSharedPreferences(
                                "discount_amount",
                                response.coupons[0].discount_amount
                            )
                            PreferenceProvider(requireActivity()).saveSharedPreferences(
                                "coupon_text",
                                response.coupons[0].coupon_text
                            )
                        } else {
                            binding.couponCodeText.text = "No coupon available"
                        }

                        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
                            val token = instanceIdResult.token
                            lifecycleScope.launch {
                                try {
                                    val saveFcmTokenResponse = viewModel.saveFcmToken(token)
                                    if (saveFcmTokenResponse.success) {
                                        dismissDialog()
                                    }
                                } catch (e: MalformedJsonException) {
                                    dismissDialog()
                                    binding.rootLayout.snackbar(e.message!!)
                                    e.printStackTrace()
                                } catch (e: com.airposted.bohon.utils.ApiException) {
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

                    } catch (e: MalformedJsonException) {
                        dismissDialog()
                        binding.rootLayout.snackbar(e.message!!)
                        e.printStackTrace()
                    } catch (e: com.airposted.bohon.utils.ApiException) {
                        dismissDialog()
                        binding.rootLayout.snackbar(e.message!!)
                        e.printStackTrace()
                    } catch (e: NoInternetException) {
                        dismissDialog()
                        binding.rootLayout.snackbar(e.message!!)
                        e.printStackTrace()
                    }
                }

            } catch (e: MalformedJsonException) {
                dismissDialog()
                binding.rootLayout.snackbar(e.message!!)
                e.printStackTrace()
            }catch (e: com.airposted.bohon.utils.ApiException) {
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

    private fun openAppNotificationSettings(context: Context) {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", context.packageName)
                    putExtra("app_uid", context.applicationInfo.uid)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.parse("package:" + context.packageName)
                }
            }
        }
        context.startActivity(intent)
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

        /*try {
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
        }*/

        viewModel.gps.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.currentLocation.observe(viewLifecycleOwner, { locationDetailsWithName ->
                    locationDetailsImp = locationDetailsWithName

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
                } else {
                    val cameraPosition =
                        CameraPosition.Builder()
                            .target(
                                LatLng(
                                    23.777176,
                                    90.399452
                                )
                            )
                            .zoom(11f)                   // Sets the zoom
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
                binding.drawerLayout.closeDrawers()
            }
            /*R.id.collected_order -> {
                myCommunicator?.addContentFragment(CollectedOrderFragment(), true)
                binding.drawerLayout.closeDrawers()
            }*/
            R.id.parcel_history -> {
                myCommunicator?.addContentFragment(MyParcelHistoryFragment(), true)
                binding.drawerLayout.closeDrawers()
            }
            R.id.cancel_order -> {
                myCommunicator?.addContentFragment(CancelOrderFragment(), true)
                binding.drawerLayout.closeDrawers()
            }
            R.id.help -> {
                myCommunicator?.addContentFragment(HelpFragment(), true)
                binding.drawerLayout.closeDrawers()
            }
            R.id.profile -> {
                myCommunicator?.addContentFragment(ProfileFragment(), true)
                binding.drawerLayout.closeDrawers()
            }
            R.id.terms_condition -> {
                binding.drawerLayout.closeDrawers()
                myCommunicator?.addContentFragment(TermsConditionsFragment(), true)
            }
            R.id.sign_out -> {
                binding.drawerLayout.closeDrawers()
                dialogs = Dialog(requireActivity())
                dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogs.setContentView(R.layout.sign_out_dialog)
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
                    setProgressDialog(requireActivity())
                    lifecycleScope.launch {
                        try {
                            val deleteFcmTokenResponse = viewModel.deleteFcmToken()
                            if (deleteFcmTokenResponse.success) {
                                dismissDialog()
                                Toast.makeText(requireActivity(), deleteFcmTokenResponse.msg, Toast.LENGTH_LONG).show()
                                PersistentUser.getInstance().logOut(context)
                                val intent = Intent(requireContext(), AuthActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                            }
                        } catch (e: com.airposted.bohon.utils.ApiException) {
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
                dialogs.setCancelable(false)
                dialogs.show()
            }
        }
        return true
    }
}