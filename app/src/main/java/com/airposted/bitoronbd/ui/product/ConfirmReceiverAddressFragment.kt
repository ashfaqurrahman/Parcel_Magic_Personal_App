package com.airposted.bitoronbd.ui.product

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.MalformedJsonException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentConfirmReceiverAddressBinding
import com.airposted.bitoronbd.model.SetParcel
import com.airposted.bitoronbd.ui.location_set.LocationSetViewModel
import com.airposted.bitoronbd.ui.location_set.LocationSetViewModelFactory
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.ui.main.MainActivity
import com.airposted.bitoronbd.ui.my_parcel.MyParcelFragment
import com.airposted.bitoronbd.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCProductInitializer
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCShipmentInfoInitializer
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCommerzInitialization
import com.sslwireless.sslcommerzlibrary.model.response.SSLCTransactionInfoModel
import com.sslwireless.sslcommerzlibrary.model.util.SSLCCurrencyType
import com.sslwireless.sslcommerzlibrary.model.util.SSLCSdkType
import com.sslwireless.sslcommerzlibrary.view.singleton.IntegrateSSLCommerz
import com.sslwireless.sslcommerzlibrary.viewmodel.listener.SSLCTransactionResponseListener
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*
import kotlin.collections.ArrayList


class ConfirmReceiverAddressFragment : Fragment(), KodeinAware, SSLCTransactionResponseListener {

    lateinit var mapFragment : SupportMapFragment
    lateinit var googleMap: GoogleMap
    private lateinit var binding: FragmentConfirmReceiverAddressBinding
    override val kodein by kodein()
    private val factory: LocationSetViewModelFactory by instance()
    private lateinit var viewModel: LocationSetViewModel
    var distance =  0F
    var myCommunicator: CommunicatorFragmentInterface? = null
    private var setParcel = SetParcel()

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

        setParcel.who_will_pay = 1

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
            val radioButtonID = binding.radioGroup.checkedRadioButtonId
            val radioButton: RadioButton = binding.radioGroup.findViewById(radioButtonID)
            val selectedtext = radioButton.text

            if (selectedtext == "Receiver"){
                submitOrder()
            } else {
                val dialogs = Dialog(requireActivity())
                dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogs.setContentView(R.layout.payment_method_dialog)
                dialogs.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialogs.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,  //w
                    ViewGroup.LayoutParams.WRAP_CONTENT //h
                )

                val collect = dialogs.findViewById<CardView>(R.id.collect)
                val collect_selected = dialogs.findViewById<RelativeLayout>(R.id.collect_selected)
                val digital = dialogs.findViewById<CardView>(R.id.digital)
                val digital_selected = dialogs.findViewById<RelativeLayout>(R.id.digital_select)
                val done_next = dialogs.findViewById<TextView>(R.id.next_done)

                collect.setOnClickListener {
                    collect_selected.visibility = View.VISIBLE
                    digital_selected.visibility = View.GONE
                    done_next.text = getString(R.string.done)
                }

                digital.setOnClickListener {
                    collect_selected.visibility = View.GONE
                    digital_selected.visibility = View.VISIBLE
                    done_next.text = getString(R.string.proceed_to_payment)
                }

                done_next.setOnClickListener {
                    dialogs.dismiss()
                    when (done_next.text) {
                        getString(R.string.done) -> {
                            dialogs.dismiss()
                            submitOrder()
                        }
                        getString(R.string.proceed_to_payment) -> {
                            val sslCommerzInitialization = SSLCommerzInitialization(
                                "centr5eb574ade72ea",
                                "centr5eb574ade72ea@ssl",
                                setParcel.delivery_charge,
                                SSLCCurrencyType.BDT,
                                setParcel.invoice_no,
                                "Shop",
                                SSLCSdkType.TESTBOX
                            )

                            /*val customerInfoInitializer = SSLCCustomerInfoInitializer(
                                addParcel.sender_name, addParcel.sender_email,
                                addParcel.sender_address, addParcel.sender_city_id.toString(), addParcel.sender_zip_code, addParcel.sender_country_id.toString(), addParcel.sender_phone
                            )*/

                            val productInitializer = SSLCProductInitializer(
                                "food", "food",
                                SSLCProductInitializer.ProductProfile.TravelVertical(
                                    "Travel", "10",
                                    "A", "12", "Dhk-Syl"
                                )
                            )

                            val shipmentInfoInitializer = SSLCShipmentInfoInitializer(
                                "Courier",
                                2, SSLCShipmentInfoInitializer.ShipmentDetails(
                                    "AA", "Address 1",
                                    "Dhaka", "1000", "BD"
                                )
                            )

                            IntegrateSSLCommerz
                                .getInstance(context)
                                .addSSLCommerzInitialization(sslCommerzInitialization)
                                //.addCustomerInfoInitializer(customerInfoInitializer)
                                //.addProductInitializer(productInitializer)
                                .buildApiCall(this)
                        }
                    }
                }

                dialogs.setCancelable(false)

                dialogs.show()
            }
            /*when {
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
            }*/
        }

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, optionId ->
            run {
                when (optionId) {
                    R.id.sender -> {
                        binding.cashOnDelivery.visibility = View.GONE
                        setParcel.who_will_pay = 1
                    }
                    R.id.receiver -> {
                        binding.cashOnDelivery.visibility = View.VISIBLE
                        setParcel.who_will_pay = 0
                    }
                }
            }
        }

        setParcel.invoice_no = "AIR" + getSaltString() + getSaltString()
        setParcel.parcel_type = 2
        setParcel.personal_order_type = 1
        setParcel.recp_name = requireArguments().getString("receiver_name")!!
        setParcel.recp_phone = requireArguments().getString("receiver_phone")!!
        setParcel.recp_city = requireArguments().getString("city")!!
        setParcel.recp_zone = requireArguments().getString("area")!!
        setParcel.recp_area = requireArguments().getString("area")!!
        setParcel.receiver_latitude = requireArguments().getDouble("latitude")
        setParcel.receiver_longitude = requireArguments().getDouble("longitude")
        setParcel.recp_address = requireArguments().getString("receiver_location_name")!!
        setParcel.pick_city = requireArguments().getString("city")!!
        setParcel.pick_zone = requireArguments().getString("area")!!
        setParcel.pick_area = requireArguments().getString("area")!!
        setParcel.sender_latitude = requireArguments().getDouble("latitude")
        setParcel.sender_longitude = requireArguments().getDouble("longitude")
        setParcel.pick_address = requireArguments().getString("sender_location_name").toString()
        setParcel.distance = round(distance.toDouble()/1000, 2)
        setParcel.delivery_charge = calculatePrice(1)
        binding.charge.text = "৳" + calculatePrice(1)
    }

    private fun getSaltString(): String {
        val SALTCHARS = "1234567890"
        val salt = StringBuilder()
        val rnd = Random()
        while (salt.length < 5) { // length of the random string.
            val index = (rnd.nextFloat() * SALTCHARS.length).toInt()
            salt.append(SALTCHARS[index])
        }
        return salt.toString()
    }

    private fun distance(): String {
        return round(requireArguments().getFloat("distance").toDouble(), 2).toString() + " Km"
    }

    private fun calculatePrice(position: Int): Double {
        return when (position) {
            0 ->round(
                ((requireArguments().getFloat("distance") * PreferenceProvider(
                    requireActivity()
                ).getSharedPreferences("rate_express")!!.toFloat()) + PreferenceProvider(
                    requireActivity()
                ).getSharedPreferences("base_price_express")!!.toFloat()).toDouble(), 2
            )
            else -> round(
                ((requireArguments().getFloat("distance") * PreferenceProvider(
                    requireActivity()
                ).getSharedPreferences("rate_quick")!!.toFloat()) + PreferenceProvider(
                    requireActivity()
                ).getSharedPreferences("base_price_quick")!!.toFloat()).toDouble(), 2
            )
        }
    }

    private fun submitOrder(){
        setProgressDialog(requireActivity())
        lifecycleScope.launch {
            try {
                val response = viewModel.setOrder(setParcel)
                dismissDialog()
                if (response.success){
                    successDialog(setParcel.invoice_no)
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

    private fun successDialog(invoice: String){
        val dialogs = Dialog(requireActivity())
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(R.layout.successful_dialog)
        dialogs.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogs.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  //w
            ViewGroup.LayoutParams.WRAP_CONTENT //h
        )

        val body = dialogs.findViewById<TextView>(R.id.body)
        val proceed = dialogs.findViewById<TextView>(R.id.proceed)
        body.text = getString(R.string.successful_order_text, invoice)

        proceed.setOnClickListener {
            dialogs.dismiss()
            requireActivity().supportFragmentManager.popBackStack(ReceiverInfoFragment::class.java.name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            myCommunicator?.addContentFragment(MyParcelFragment(), true)

        }

        dialogs.setCancelable(false)

        dialogs.show()
    }

    override fun transactionSuccess(p0: SSLCTransactionInfoModel) {
        setParcel.ssl_transaction_id = p0.bankTranId
        submitOrder()
    }

    override fun transactionFail(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun merchantValidationError(p0: String?) {
        TODO("Not yet implemented")
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