package com.airposted.bitoronbd.ui.product

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.MalformedJsonException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentConfirmDeliveryRequestBinding
import com.airposted.bitoronbd.model.SetParcel
import com.airposted.bitoronbd.ui.my_parcel.MyParcelViewModel
import com.airposted.bitoronbd.ui.my_parcel.MyParcelViewModelFactory
import com.airposted.bitoronbd.utils.*
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


class ConfirmDeliveryRequestFragment : Fragment(), KodeinAware, SSLCTransactionResponseListener {
    override val kodein by kodein()
    private lateinit var binding: FragmentConfirmDeliveryRequestBinding
    private val factory: MyParcelViewModelFactory by instance()
    private lateinit var viewModel: MyParcelViewModel
    private var finalCost = ""
    private var invoice = ""
    private var setParcel = SetParcel()
    private var paymentTye = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConfirmDeliveryRequestBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), factory).get(MyParcelViewModel::class.java)
        bindUI()
    }

    private fun bindUI() {

        setParcel.who_will_pay = 1

        val deliveryType = arrayOf(
            "Express",
            "Quick"
        )

        val adapter =
            ArrayAdapter(requireActivity(), R.layout.row, R.id.text_view_name, deliveryType)
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    //setParcel.delivery_type = 1
                } else {
                    //setParcel.delivery_type = 2
                }
                finalCost = "৳" + calculatePrice(position)
                setParcel.delivery_charge = calculatePrice(position)
                binding.charge.text = finalCost
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
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

        binding.toolbar.toolbarTitle.text = getString(R.string.one_final_step)

        setParcel.invoice_no = "AIR" + getSaltString() + getSaltString()
        setParcel.parcel_type = 2
        setParcel.recp_name = requireArguments().getString("name")!!
        setParcel.recp_phone = requireArguments().getString("phone")!!
        setParcel.recp_city = requireArguments().getString("city")!!
        setParcel.recp_zone = requireArguments().getString("area")!!
        setParcel.recp_area = requireArguments().getString("area")!!
        setParcel.receiver_latitude = requireArguments().getDouble("latitude")
        setParcel.receiver_longitude = requireArguments().getDouble("longitude")
        setParcel.recp_address = requireArguments().getString("location_name")!!
        setParcel.pick_city = requireArguments().getString("city")!!
        setParcel.pick_zone = requireArguments().getString("area")!!
        setParcel.pick_area = requireArguments().getString("area")!!
        setParcel.sender_latitude = requireArguments().getDouble("latitude")
        setParcel.sender_longitude = requireArguments().getDouble("longitude")
        setParcel.pick_address = PreferenceProvider(
            requireActivity()
        ).getSharedPreferences("currentLocation")!!
        setParcel.distance = round(requireArguments().getFloat("distance").toDouble(), 2)

        binding.name.text = requireArguments().getString("name")
        binding.phone.text = requireArguments().getString("phone")

        binding.from.text = requireArguments().getString("location_name")
        binding.to.text = PreferenceProvider(
            requireActivity()
        ).getSharedPreferences("currentLocation")
        binding.distance.text = distance()

        binding.confirmDeliveryRequest.setOnClickListener {

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

        }
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

}

