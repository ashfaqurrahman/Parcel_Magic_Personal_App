package com.airposted.bitoronbd.ui.base

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat


open class BaseActivity : Activity(), LocationListener {
    private var mLocationManager: LocationManager? = null
    private val mNetworkDetectReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkInternetConnection()
        }
    }
    private var mInternetDialog: AlertDialog? = null
    private var mGPSDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0F, this)
        registerReceiver(
            mNetworkDetectReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    override fun onDestroy() {
        mLocationManager!!.removeUpdates(this)
        unregisterReceiver(mNetworkDetectReceiver)
        super.onDestroy()
    }

    private fun checkInternetConnection() {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = manager.activeNetworkInfo
        if (ni != null && ni.state == NetworkInfo.State.CONNECTED) {
        } else {
            showNoInternetDialog()
        }
    }

    override fun onLocationChanged(location: Location) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {
        if (provider == LocationManager.GPS_PROVIDER) {
            showGPSDiabledDialog()
        }
    }

    private fun showNoInternetDialog() {
        if (mInternetDialog != null && mInternetDialog!!.isShowing()) {
            return
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Internet Disabled!")
        builder.setMessage("No active Internet connection found.")
        builder.setPositiveButton("Turn On",
            DialogInterface.OnClickListener { dialog, which ->
                val gpsOptionsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivityForResult(gpsOptionsIntent, WIFI_ENABLE_REQUEST)
            }).setNegativeButton("No, Just Exit",
            DialogInterface.OnClickListener { dialog, which -> })
        mInternetDialog = builder.create()
        mInternetDialog!!.show()
    }

    fun showGPSDiabledDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("GPS Disabled")
        builder.setMessage("Gps is disabled, in order to use the application properly you need to enable GPS of your device")
        builder.setPositiveButton("Enable GPS",
            DialogInterface.OnClickListener { dialog, which ->
                startActivityForResult(
                    Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    ), GPS_ENABLE_REQUEST
                )
            }).setNegativeButton("No, Just Exit",
            DialogInterface.OnClickListener { dialog, which -> })
        mGPSDialog = builder.create()
        mGPSDialog!!.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == GPS_ENABLE_REQUEST) {
            if (mLocationManager == null) {
                mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            }
            if (!mLocationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGPSDiabledDialog()
            }
        } else if (requestCode == WIFI_ENABLE_REQUEST) {
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val GPS_ENABLE_REQUEST = 0x1001
        private const val WIFI_ENABLE_REQUEST = 0x1006
    }
}