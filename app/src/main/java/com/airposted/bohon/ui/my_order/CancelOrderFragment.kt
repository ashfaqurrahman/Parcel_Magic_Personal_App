package com.airposted.bohon.ui.my_order

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.MalformedJsonException
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.airposted.bohon.databinding.FragmentCancelOrderBinding
import com.airposted.bohon.model.DataX
import com.airposted.bohon.ui.main.CommunicatorFragmentInterface
import com.airposted.bohon.utils.*
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class CancelOrderFragment : Fragment(), KodeinAware, OrderClickListener {
    override val kodein by kodein()
    private lateinit var binding: FragmentCancelOrderBinding
    private val factory: MyParcelViewModelFactory by instance()
    private lateinit var viewModel: MyParcelViewModel
    private lateinit var invoice:List<DataX>
    private var currentItemPosition = 0
    private var selectedItemPosition = 0
    var communicatorFragmentInterface: CommunicatorFragmentInterface? = null
    private lateinit var dataList:List<DataX>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCancelOrderBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), factory).get(MyParcelViewModel::class.java)
        bindUI()
    }

    private fun bindUI() {
        communicatorFragmentInterface = context as CommunicatorFragmentInterface
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
            binding.expressQuick.dismiss()
        }

        getOrderList(0+6)

        binding.expressQuick.setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
            run {
                if (oldIndex != newIndex) {
                    getOrderList(newIndex+6)
                }
            }
        })

        /*binding.searchItem.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                binding.searchItem.setText("#")
                binding.searchItem.showKeyboard()
                Selection.setSelection(binding.searchItem.text, binding.searchItem.length())
            } else {
                binding.searchItem.hideKeyboard()
                if (binding.searchItem.text.length == 1) {
                    binding.searchItem.setText("")
                }
            }
        }*/

        binding.searchItem.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                /*if (!s.toString().contains("#")) {
                    binding.searchItem.setText("#")
                    Selection.setSelection(binding.searchItem.text, binding.searchItem.text.length)
                }*/
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {

                binding.orders.layoutManager = GridLayoutManager(
                    requireActivity(),
                    1
                )
                binding.orders.itemAnimator = DefaultItemAnimator()
                if (s.toString().isNotEmpty()) {
                    val listNew: ArrayList<DataX> = ArrayList()
                    for (l in invoice.indices) {
                        val serviceName: String = invoice[l].invoice_no
                        if (serviceName.contains(s.toString())) {
                            listNew.add(invoice[l])
                            binding.orders.visibility = View.VISIBLE
                            binding.noOrder.visibility = View.GONE
                        }
                    }
                    if (listNew.isNullOrEmpty()) {
                        binding.orders.visibility = View.GONE
                        binding.noOrder.visibility = View.VISIBLE
                    } else {
                        val myRecyclerViewAdapter = OrderListRecyclerViewAdapter(
                            listNew,
                            requireActivity(),
                            this@CancelOrderFragment
                        )
                        binding.orders.adapter = myRecyclerViewAdapter
                    }
                } else {
                    binding.orders.visibility = View.VISIBLE
                    binding.noOrder.visibility = View.GONE
                    val myRecyclerViewAdapter = OrderListRecyclerViewAdapter(
                        invoice,
                        requireActivity(),
                        this@CancelOrderFragment
                    )
                    binding.orders.adapter = myRecyclerViewAdapter
                }
            }
        })
    }

    private fun getOrderList(order: Int) {
        setProgressDialog(requireActivity())
        lifecycleScope.launch {
            try {
                val response = viewModel.getOrderList(order)
                invoice = response.data
                if (response.data.isNotEmpty()) {
                    binding.orders.visibility = View.VISIBLE
                    binding.noOrder.visibility = View.GONE
                    val myRecyclerViewAdapter = OrderListRecyclerViewAdapter(
                        response.data,
                        requireActivity(),
                        this@CancelOrderFragment
                    )
                    binding.orders.layoutManager = GridLayoutManager(
                        requireActivity(),
                        1
                    )
                    binding.orders.itemAnimator = DefaultItemAnimator()
                    binding.orders.adapter = myRecyclerViewAdapter
                } else {
                    binding.orders.visibility = View.GONE
                    binding.noOrder.visibility = View.VISIBLE
                }
                dismissDialog()
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

    override fun onItemClick(order: DataX) {
        val fragment = OrderDetailsFragment()
        val bundle = Bundle()
        bundle.putString("personal_order_type", order.personal_order_type)
        bundle.putInt("item_type", order.item_type)
        bundle.putString("item_qty", order.item_qty)
        bundle.putInt("distance", order.distance)
        bundle.putString("recp_name", order.recp_name)
        bundle.putString("recp_phone", order.recp_phone)
        bundle.putString("recp_address", order.recp_address)
        bundle.putString("pick_address", order.pick_address)
        bundle.putDouble("sender_latitude", order.sender_latitude)
        bundle.putDouble("sender_longitude", order.sender_longitude)
        bundle.putDouble("receiver_latitude", order.receiver_latitude)
        bundle.putDouble("receiver_longitude", order.receiver_longitude)
        bundle.putInt("current_status", order.current_status)
        bundle.putInt("delivery_charge", order.delivery_charge)
        bundle.putString("invoice", order.invoice_no)
        bundle.putString("order_date", order.order_date)
        fragment.arguments = bundle
        communicatorFragmentInterface?.addContentFragment(fragment, true)
    }
}