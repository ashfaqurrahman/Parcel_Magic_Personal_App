package com.airposted.bitoronbd.ui.main

import android.app.AlertDialog
import android.content.*
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.ActivityMainBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.lang.reflect.Field
import java.lang.reflect.Method


class MainActivity : AppCompatActivity(), KodeinAware, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    private lateinit var mainBinding: ActivityMainBinding
    override val kodein by kodein()

    private val factory: MainViewModelFactory by instance()
    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel
    private var mInternetDialog: AlertDialog? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var result: PendingResult<LocationSettingsResult>? = null
    private var wifiManager: WifiManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.productFragment) {
                mainBinding.bottomNavigation.visibility = View.GONE
            } else {
                mainBinding.bottomNavigation.visibility = View.VISIBLE
            }
        }

        bindUI()

        mainBinding.bottomNavigation.setupWithNavController(navController)
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun bindUI() {

        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        viewModel.gps.observe(this, {
            if (!it) {
                mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build()
                mGoogleApiClient?.connect()
            } else {

            }
        })

        viewModel.currentLocation.observe(this, {
            Log.e("aaaaa", it)
        })

        viewModel.network.observe(this, { itt ->
            if (!itt) {
                mainBinding.noInternet.visibility = View.VISIBLE
                showNoInternetDialog()
            } else {
                if (mInternetDialog != null) {
                    mainBinding.noInternet.visibility = View.GONE
                    mInternetDialog!!.dismiss()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_navigation, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun showNoInternetDialog() {
        if (mInternetDialog != null && mInternetDialog!!.isShowing) {
            return
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Internet Disabled!")
        builder.setMessage("No active Internet connection found.")
        builder.setPositiveButton(
            "Turn On"
        ) { _, _ ->
            //val gpsOptionsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
            //startActivityForResult(gpsOptionsIntent, WIFI_ENABLE_REQUEST)
            setMobileDataEnabled()
        }
        mInternetDialog = builder.create()
        mInternetDialog!!.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            GPS_ENABLE_REQUEST -> {
                viewModel.gps.observe(this, {
                    if (!it) {
                        val manager: LocationManager =
                            getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            mGoogleApiClient = GoogleApiClient.Builder(this)
                                .addApi(LocationServices.API)
                                .addConnectionCallbacks(this)
                                .addOnConnectionFailedListener(this).build()
                            mGoogleApiClient?.connect()
                        }
                    } else {
                        viewModel.network.observe(this@MainActivity, { itt ->
                            if (!itt) {
                                mainBinding.noInternet.visibility = View.VISIBLE
                                showNoInternetDialog()
                            } else {
                                if (mInternetDialog != null) {
                                    mainBinding.noInternet.visibility = View.GONE
                                    mInternetDialog!!.dismiss()
                                }
                            }
                        })
                    }
                })
            }
            WIFI_ENABLE_REQUEST -> {

            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    companion object {
        private const val GPS_ENABLE_REQUEST = 0x1001
        private const val WIFI_ENABLE_REQUEST = 0x1006
    }

    override fun onConnected(p0: Bundle?) {
        val mLocationRequest = LocationRequest.create()
        val builder: LocationSettingsRequest.Builder =
            LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        builder.setAlwaysShow(true);
        result = LocationServices.SettingsApi.checkLocationSettings(
            mGoogleApiClient,
            builder.build()
        )

        result?.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {

                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->  // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try { // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(
                            this@MainActivity,
                            GPS_ENABLE_REQUEST
                        )
                    } catch (e: IntentSender.SendIntentException) { // Ignore the error.
                    }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    private fun setMobileDataEnabled() {
        wifiManager?.isWifiEnabled = true

    }
}