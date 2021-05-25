package com.airposted.bitoronbd.ui.my_order

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.MalformedJsonException
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentOrderDetailsBinding
import com.airposted.bitoronbd.ui.home.HomeFragment
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.utils.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class OrderDetailsFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory: MyParcelViewModelFactory by instance()
    private lateinit var binding: FragmentOrderDetailsBinding
    private lateinit var viewModel: MyParcelViewModel
    var communicatorFragmentInterface: CommunicatorFragmentInterface? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, factory).get(MyParcelViewModel::class.java)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        communicatorFragmentInterface = context as CommunicatorFragmentInterface
        binding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.toolbar.toolbarTitle.text = getString(R.string.order_detaisl)
        if (requireArguments().getString("this") == "placeOrder") {
            binding.cancelLayout.visibility = View.VISIBLE
        }

        binding.deliveryType.text = requireArguments().getString("personal_order_type").toString() + " Delivery"
        binding.invoice.text = "#" + requireArguments().getString("invoice")
        binding.quantity.text = requireArguments().getString("item_qty")
        binding.distance.text = requireArguments().getInt("distance").toString() + " km"
        binding.charge.text = "BDT " + requireArguments().getInt("delivery_charge").toString()
        binding.deliveryDate.text = requireArguments().getString("order_date")
        binding.receiverName.text = requireArguments().getString("recp_name")
        binding.receiverPhone.text = requireArguments().getString("recp_phone")
        binding.senderName.text = PersistentUser.getInstance().getFullName(requireContext())
        binding.senderPhone.text = PersistentUser.getInstance().getPhoneNumber(requireContext())
        binding.from.text = requireArguments().getString("pick_address")
        binding.to.text = requireArguments().getString("recp_address")

        when (requireArguments().getInt("current_status")) {
            2 -> {
                binding.status.text = "Pending"
            }
            3 -> {
                binding.status.text = "Accepted"
            }
            4 -> {
                binding.status.text = "Collected"
            }
            5 -> {
                binding.status.text = "Delivered"
            }
            13 -> {
                binding.status.text = "Cancelled"
            }
        }

        when(requireArguments().getInt("item_type")) {
            1 -> {
                binding.size.text = getString(R.string.envelope_size1)
                binding.icon.setBackgroundResource(R.drawable.ic_document_large_icon)
            }
            2 -> {
                binding.size.text = getString(R.string.small_size1)
                binding.icon.setBackgroundResource(R.drawable.ic_box_large_icon)
            }
            3 -> {
                binding.size.text = getString(R.string.large_size1)
                binding.icon.setBackgroundResource(R.drawable.ic_box_large_icon)
            }
        }

        binding.cancelOrder.setOnClickListener {
            val dialogs = Dialog(requireActivity())
            dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogs.setContentView(R.layout.cancel_order_dialog)
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
                dialogs.dismiss()
                setProgressDialog(requireActivity())
                lifecycleScope.launch {
                    try {
                        val response = viewModel.changeStatus(requireArguments().getString("invoice")!!, 13)
                        if (response.success) {
                            dismissDialog()
                            communicatorFragmentInterface!!.addContentFragment(HomeFragment(), true)
                        }
                        else {
                            binding.rootLayout.snackbar(response.msg)
                            dismissDialog()
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

            dialogs.setCancelable(true)
            dialogs.show()
        }
    }
}