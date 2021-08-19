package com.airposted.bohon.ui.create_parcel

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bohon.R
import com.airposted.bohon.data.network.preferences.PreferenceProvider
import com.airposted.bohon.databinding.FragmentConfirmReceiverAddressBinding
import com.airposted.bohon.model.SetParcel
import com.airposted.bohon.ui.location_set.LocationSetViewModel
import com.airposted.bohon.ui.location_set.LocationSetViewModelFactory
import com.airposted.bohon.ui.main.CommunicatorFragmentInterface
import com.airposted.bohon.ui.my_order.MyParcelFragment
import com.airposted.bohon.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.stream.MalformedJsonException
import com.skydoves.powerspinner.PowerSpinnerView
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

    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap
    private lateinit var binding: FragmentConfirmReceiverAddressBinding
    override val kodein by kodein()
    private val factory: LocationSetViewModelFactory by instance()
    private lateinit var viewModel: LocationSetViewModel
    var distance = 0
    var charge = 0.0
    var myCommunicator: CommunicatorFragmentInterface? = null
    private var setParcel = SetParcel()
    var communicatorFragmentInterface: CommunicatorFragmentInterface? = null

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
        communicatorFragmentInterface = context as CommunicatorFragmentInterface
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        setParcel.who_will_pay = 1

        setProgressDialog(requireActivity())
        myCommunicator = context as CommunicatorFragmentInterface

        mapFragment =
            childFragmentManager.findFragmentById(R.id.mapReceiverDetails) as SupportMapFragment
        mapFragment.getMapAsync { it ->
            googleMap = it
            googleMap.uiSettings.isZoomControlsEnabled = false
            googleMap.uiSettings.isZoomGesturesEnabled = false
            googleMap.uiSettings.isRotateGesturesEnabled = false
            googleMap.uiSettings.isScrollGesturesEnabled = false

            googleMap.setOnMarkerClickListener {
                false
            }
//            googleMap.isMyLocationEnabled = true
            val location1 = LatLng(
                requireArguments().getDouble("sender_latitude"),
                requireArguments().getDouble("sender_longitude")
            )

            val location2 = LatLng(
                requireArguments().getDouble("receiver_latitude"),
                requireArguments().getDouble("receiver_longitude")
            )


            val bounds = LatLngBounds.Builder()
            bounds.include(location1)
            bounds.include(location2)

            googleMap.setOnMarkerClickListener {
                true
            }

            lifecycleScope.launch {
                try {
                    val url = getDirectionURL(location1, location2)
                    val list = viewModel.getDirections(url)
                    val result = ArrayList<List<LatLng>>()
                    val path = ArrayList<LatLng>()
                    for (i in 0 until list.routes[0].legs[0].steps.size) {
                        path.addAll(decodePolyline(list.routes[0].legs[0].steps[i].polyline.points))
                    }
                    result.add(path)
                    val lineoption = PolylineOptions()
                    for (i in result.indices) {
                        lineoption.addAll(result[i])
                        lineoption.width(6f)
                        lineoption.color(resources.getColor(R.color.blue))
                        lineoption.geodesic(true)
                    }
                    googleMap.addPolyline(lineoption)
                    /*for (i in 0 until list.routes[0].legs[0].steps.size){
                        distance += list.routes[0].legs[0].steps[i].distance.value
                    }*/
                    distance = list.routes[0].legs[0].distance.value

                    setParcel.distance = round((distance / 1000).toDouble(), 2)
                    charge = calculatePrice(requireArguments().getInt("delivery_type"))
                    setParcel.delivery_charge = charge
                    binding.charge.text = "Tk $charge"
                    binding.total.text = "Tk $charge"

                    viewModel.couponPrice.observe(viewLifecycleOwner, ) {
                        if (it != null) {
                            val newCharge = calculatePrice(requireArguments().getInt("delivery_type")) - it.coupons!!.discount_amount.toInt()
                            setParcel.delivery_charge = newCharge
                            binding.total.text = "Tk $newCharge"
                            binding.discountAmount.text = "Tk -${it.coupons.discount_amount}"
                            binding.couponText.text = it.coupons.coupon_text
                            binding.rootLayout.snackbar("Coupon Added")
                            binding.applyCoupon.visibility = View.GONE
                            binding.couponLayout.visibility = View.VISIBLE
                        }
                    }

                    val circleDrawable = resources.getDrawable(R.drawable.root_start_point)
                    val markerIcon = getMarkerIconFromDrawable(circleDrawable)
                    googleMap.addMarker(
                        MarkerOptions().position(location1)
                            .icon(markerIcon)
                            .title(requireArguments().getString("sender_location_name"))
                    ).showInfoWindow()

                    /*googleMap.setInfoWindowAdapter(object : InfoWindowAdapter {
                        override fun getInfoWindow(marker: Marker?): View? {
                            return null
                        }

                        override fun getInfoContents(marker: Marker): View {
                            val v: View = layoutInflater.inflate(R.layout.row, null)
                            val info1 = v.findViewById(R.id.text_view_name) as TextView
                            info1.text = requireArguments().getString("receiver_location_name")
                            googleMap.setOnInfoWindowClickListener {
                                val fragmento: Fragment
                                *//*fragmento = HistorialFragment()
                                val bundle = Bundle()
                                bundle.putString("title", info1.text.toString())
                                fragmento.arguments = bundle
                                getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.content_principal, fragmento)
                                    .commit()*//*
                            }
                            return v
                        }
                    })*/

                    val circleDrawable1 = resources.getDrawable(R.drawable.ic_marker)
                    val markerIcon1 = getMarkerIconFromDrawable(circleDrawable1)
                    googleMap.addMarker(
                        MarkerOptions().position(location2)
                            .icon(markerIcon1)
                            .title(requireArguments().getString("receiver_location_name"))
                    ).showInfoWindow()

                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            bounds.build(),
                            mapFragment.requireView().width - 300,
                            mapFragment.requireView().height - 300,
                            (mapFragment.requireView().height * 0.1f).toInt()
                        )
                    )

                    /*if (distance/1000 > 5){
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location1, 12f))
                    } else {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location1, 13f))
                    }*/
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

        binding.applyCoupon.setOnClickListener {
            val fragment = CouponFragment()
            communicatorFragmentInterface?.addContentFragment(fragment, true)
        }

        binding.removeCoupon.setOnClickListener {
            viewModel.couponPriceUpdate
            charge = calculatePrice(requireArguments().getInt("delivery_type"))
            setParcel.delivery_charge = charge
            binding.total.text = "Tk $charge"
            binding.applyCoupon.visibility = View.VISIBLE
            binding.couponLayout.visibility = View.GONE
        }

        binding.confirmDelivery.setOnClickListener {
            val radioButtonID = binding.radioGroup.checkedRadioButtonId
            val radioButton: RadioButton = binding.radioGroup.findViewById(radioButtonID)
            val selectedtext = radioButton.text

            if (selectedtext == "Recipient") {
                submitOrder()
            } else {
                when (binding.confirmDeliveryText.text) {
                    getString(R.string.confirm_delivery) -> {
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

        binding.paymentMethod.setOnCheckedChangeListener { radioGroup, optionId ->
            run {
                when (optionId) {
                    R.id.coc -> {
                        binding.confirmDeliveryText.text = getString(R.string.confirm_delivery)
                    }
                    R.id.cod -> {
                        binding.confirmDeliveryText.text = getString(R.string.proceed_to_payment)
                    }
                }
            }
        }

        setParcel.invoice_no = "AIR" + getSaltString() + getSaltString()
        setParcel.item_type = requireArguments().getInt("parcel_type")
        setParcel.personal_order_type = requireArguments().getInt("delivery_type")
        setParcel.item_qty = requireArguments().getInt("parcel_quantity")
        setParcel.recp_name = requireArguments().getString("receiver_name")!!
        setParcel.recp_phone = requireArguments().getString("receiver_phone")!!
        setParcel.pic_name = PersistentUser.getInstance().getFullName(requireContext())
        setParcel.pic_phone = PersistentUser.getInstance().getPhoneNumber(requireContext())
//        setParcel.recp_city = requireArguments().getString("city")!!
//        setParcel.recp_zone = requireArguments().getString("area")!!
//        setParcel.recp_area = requireArguments().getString("area")!!
        setParcel.receiver_latitude = requireArguments().getDouble("receiver_latitude")
        setParcel.receiver_longitude = requireArguments().getDouble("receiver_longitude")
        setParcel.recp_address = requireArguments().getString("receiver_location_name")!!
//        setParcel.pick_city = requireArguments().getString("city")!!
//        setParcel.pick_zone = requireArguments().getString("area")!!
//        setParcel.pick_area = requireArguments().getString("area")!!
        setParcel.sender_latitude = requireArguments().getDouble("sender_latitude")
        setParcel.sender_longitude = requireArguments().getDouble("sender_longitude")
        setParcel.pick_address = requireArguments().getString("sender_location_name")!!
    }

    private fun showCouponDialog() {
        val dialogs = Dialog(requireActivity())
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(R.layout.coupon_dialog)
        dialogs.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogs.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  //w
            ViewGroup.LayoutParams.MATCH_PARENT //h
        )
        val backImage = dialogs.findViewById<ImageView>(R.id.backImage)
        val couponText = dialogs.findViewById<EditText>(R.id.coupon_text)
        val apply = dialogs.findViewById<TextView>(R.id.apply)
        apply.setOnClickListener {
            if (couponText.text.toString().isNotEmpty()) {
                setProgressDialog(requireContext())
                lifecycleScope.launch {
                    try {
                        hideKeyboard(requireActivity())
                        val response = viewModel.checkCoupon(couponText.text.toString())
                        if (response.message == "Successful") {
                            val newCharge =
                                calculatePrice(requireArguments().getInt("delivery_type")) - response.coupons!!.discount_amount.toInt()
                            setParcel.delivery_charge = newCharge
                            binding.total.text = "Tk $newCharge"
                            binding.discountAmount.text = "Tk -${response.coupons.discount_amount}"
                            binding.couponText.text = couponText.text.toString()
                            binding.rootLayout.snackbar(response.message)
                            binding.applyCoupon.visibility = View.GONE
                            binding.couponLayout.visibility = View.VISIBLE
                            dismissDialog()
                            dialogs.dismiss()
                        } else {
                            charge = calculatePrice(requireArguments().getInt("delivery_type"))
                            setParcel.delivery_charge = charge
                            binding.total.text = "Tk $charge"
                            binding.applyCoupon.visibility = View.VISIBLE
                            binding.couponLayout.visibility = View.GONE
                            binding.rootLayout.snackbar(response.message)
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
        }
        backImage.setOnClickListener {
            dialogs.dismiss()
        }
        dialogs.setCancelable(true)
        dialogs.show()
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

    /*private fun distance(): String {
        return round(requireArguments().getFloat("distance").toDouble(), 2).toString() + " Km"
    }*/

    private fun calculatePrice(position: Int): Double {
        return when (position) {
            1 -> round(
                (requireArguments().getInt("parcel_quantity") * ((distance / 1000 * PreferenceProvider(
                    requireActivity()
                ).getSharedPreferences("per_km_price_quick")!!.toInt()) + PreferenceProvider(
                    requireActivity()
                ).getSharedPreferences("base_price_quick")!!.toInt())).toDouble(), 2
            )
            else -> round(
                (requireArguments().getInt("parcel_quantity") * ((distance / 1000 * PreferenceProvider(
                    requireActivity()
                ).getSharedPreferences("per_km_price_express")!!.toInt()) + PreferenceProvider(
                    requireActivity()
                ).getSharedPreferences("base_price_express")!!.toInt())).toDouble(), 2
            )
        }
    }

    private fun submitOrder() {
        setProgressDialog(requireActivity())
        lifecycleScope.launch {
            try {
                val response = viewModel.setOrder(setParcel)
                dismissDialog()
                if (response.success) {
                    successDialog(setParcel.invoice_no)
                }

            } catch (e: com.google.gson.stream.MalformedJsonException) {
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

    private fun successDialog(invoice: String) {
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
            requireActivity().supportFragmentManager.popBackStack(
                ParcelTypeFragment::class.java.name,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
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
        binding.rootLayout.snackbar("Create Parcel Failed!")
    }

    override fun merchantValidationError(p0: String?) {
        binding.rootLayout.snackbar("Create Parcel Failed!")
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

    private fun getDirectionURL(origin: LatLng, dest: LatLng): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&key=AIzaSyAJnceVASls_tIv4MiZFkzY1ZrVgu6GmW4&sensor=false&mode=driving"
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