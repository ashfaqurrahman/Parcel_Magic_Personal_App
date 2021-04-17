package com.airposted.bitoronbd.ui.my_parcel

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.Log
import android.util.MalformedJsonException
import android.view.*
import android.view.View.OnFocusChangeListener
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentMyParcelBinding
import com.airposted.bitoronbd.model.DataX
import com.airposted.bitoronbd.ui.adapter.SimpleTextAdapter
import com.airposted.bitoronbd.ui.data.MenuItemData
import com.airposted.bitoronbd.ui.widget.CursorWheelLayout
import com.airposted.bitoronbd.ui.widget.SimpleTextCursorWheelLayout
import com.airposted.bitoronbd.ui.widget.SwitchButton
import com.airposted.bitoronbd.utils.*
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class MyParcelFragment : Fragment(), KodeinAware, CursorWheelLayout.OnMenuSelectedListener {
    override val kodein by kodein()
    private lateinit var binding: FragmentMyParcelBinding
    private val factory: MyParcelViewModelFactory by instance()
    private lateinit var viewModel: MyParcelViewModel
    private var currentItemPosition = 0
    private var selectedItemPosition = 0
    private lateinit var invoice:List<DataX>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyParcelBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), factory).get(MyParcelViewModel::class.java)
        bindUI()
    }

    private fun bindUI() {
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
            binding.expressQuick.dismiss()
        }

        getOrderList(0)

        binding.expressQuick.setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
            run {
                if (oldIndex != newIndex) {
                    getOrderList(newIndex)
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
                            listNew
                        )
                        binding.orders.adapter = myRecyclerViewAdapter
                    }
                } else {
                    binding.orders.visibility = View.VISIBLE
                    binding.noOrder.visibility = View.GONE
                    val myRecyclerViewAdapter = OrderListRecyclerViewAdapter(
                        invoice
                    )
                    binding.orders.adapter = myRecyclerViewAdapter
                }
            }
        })
    }

    override fun onItemSelected(
        parent: CursorWheelLayout?,
        view: View?,
        pos: Int
    ) {

    }

    private fun getOrderList(order: Int) {
        when(order){
            0 -> binding.title.text = "Current Orders"
            1 -> binding.title.text = "Current Orders"
            2 -> binding.title.text = "Order History"
            3 -> binding.title.text = "Order History"
        }
        setProgressDialog(requireActivity())
        lifecycleScope.launch {
            try {
                val response = viewModel.getOrderList(order)
                invoice = response.data
                if (response.data.isNotEmpty()) {
                    binding.orders.visibility = View.VISIBLE
                    binding.noOrder.visibility = View.GONE
                    val myRecyclerViewAdapter = OrderListRecyclerViewAdapter(
                        response.data
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

    private fun openNotificationDialog() {
        val orderDialog = Dialog(requireContext())
        orderDialog.setContentView(R.layout.order_dialog)

        val testCircleMenuLeft = orderDialog.findViewById<SimpleTextCursorWheelLayout>(R.id.test_circle_menu_left)
        val idWheelMenuCenterItem = orderDialog.findViewById<SwitchButton>(R.id.id_wheel_menu_center_item)

        val res = arrayOf(
            "Current Orders",
            "Completed Orders",
            "Cancelled Orders",
            "Current Orders",
            "Completed Orders",
            "Cancelled Orders"
        )
        val menuItemDatas: MutableList<MenuItemData> = ArrayList()
        for (i in res.indices) {
            menuItemDatas.add(MenuItemData(res[i]))
        }
        val simpleTextAdapter =
            SimpleTextAdapter(
                requireActivity(),
                menuItemDatas,
                Gravity.TOP or Gravity.END
            )
        testCircleMenuLeft.setAdapter(simpleTextAdapter)
        testCircleMenuLeft.setSelection(currentItemPosition)
        /*testCircleMenuLeft.setOnMenuSelectedListener { parent, view, pos ->
            Toast.makeText(
                requireActivity(),
                "Top Menu click position:$pos",
                Toast.LENGTH_SHORT
            ).show()
            selectedItemPosition = pos
        }*/
        testCircleMenuLeft.setOnMenuItemClickListener { _, pos ->
            selectedItemPosition = pos
        }

        testCircleMenuLeft.setOnMenuSelectedListener { _, view, pos ->
            selectedItemPosition = pos
        }

        /*idWheelMenuCenterItem.setOnClickListener {
            if (currentItemPosition == selectedItemPosition){
                orderDialog.dismiss()
            } else {
                orderDialog.dismiss()
                currentItemPosition = selectedItemPosition
                getOrderList(selectedItemPosition)
            }
        }*/

        val window: Window? = orderDialog.window
        val wlp = window?.attributes

        wlp?.gravity = Gravity.TOP
        orderDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        orderDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        orderDialog.setCancelable(true)
        orderDialog.show()
    }

}