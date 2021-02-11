package com.airposted.bitoronbd.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentHomeBinding
import com.airposted.bitoronbd.utils.Coroutines
import com.google.android.gms.location.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.properties.Delegates


class HomeFragment : Fragment(R.layout.fragment_home), KodeinAware {

    private lateinit var homeBinding: FragmentHomeBinding
    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()
    private lateinit var viewModel: HomeViewModel
    private var latitude by Delegates.notNull<Double>()
    private var longitude by Delegates.notNull<Double>()

    var mLocationRequest: LocationRequest? = null
    var mLastLocation: Location? = null
    var mFusedLocationClient: FusedLocationProviderClient? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)

        return homeBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), factory).get(HomeViewModel::class.java)
        bindUI()
    }

    private fun bindUI() = Coroutines.main {
        homeBinding.productBtn.setOnClickListener{
            Navigation.findNavController(requireView()).navigate(
                R.id.productFragment
            )
        }

        /*mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mLocationRequest = LocationRequest()

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback, Looper.myLooper()
        )*/

    }

    var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                val location = locationList[locationList.size - 1]
                mLastLocation = location
                val geo = Geocoder(requireActivity(), Locale.getDefault())
                val addresses = geo.getFromLocation(location.latitude, location.longitude, 1);
                if (addresses.isEmpty()) {
                    homeBinding.address.text = getString(R.string.searching)
                }
                else {
                    homeBinding.address.text =
                        addresses[0].featureName + ", " + addresses[0].thoroughfare
                }
            }
        }
    }
}