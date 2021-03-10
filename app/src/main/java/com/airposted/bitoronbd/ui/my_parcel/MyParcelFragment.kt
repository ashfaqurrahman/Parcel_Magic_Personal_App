package com.airposted.bitoronbd.ui.my_parcel

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentMyParcelBinding
import com.airposted.bitoronbd.model.Response
import com.airposted.bitoronbd.ui.adapter.SimpleTextAdapter
import com.airposted.bitoronbd.ui.data.MenuItemData
import com.airposted.bitoronbd.ui.widget.CursorWheelLayout
import com.airposted.bitoronbd.ui.widget.SimpleTextCursorWheelLayout
import com.airposted.bitoronbd.ui.widget.SwitchButton
import com.airposted.bitoronbd.utils.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import kotlin.collections.ArrayList

class MyParcelFragment : Fragment(), KodeinAware, CursorWheelLayout.OnMenuSelectedListener {
    override val kodein by kodein()
    private lateinit var binding: FragmentMyParcelBinding
    private val factory: MyParcelViewModelFactory by instance()
    private lateinit var viewModel: MyParcelViewModel
    private var currentItemPosition = 0
    private var selectedItemPosition = 0
    private lateinit var invoice:List<Response>

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
        binding.menu.setOnClickListener {
            openNotificationDialog()
        }
        getOrderList(0)

        binding.searchItem.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

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
                if (s.toString().isNotEmpty()) {
                    val listNew: ArrayList<Response> = ArrayList()
                    for (l in invoice.indices) {
                        val serviceName: String = invoice[l].invoiceNo
                        if (serviceName.contains(s.toString())) {
                            listNew.add(invoice[l])
                        }
                    }
                    val myRecyclerViewAdapter = OrderListRecyclerViewAdapter(
                        listNew
                    )
                    binding.orders.layoutManager = GridLayoutManager(
                        requireActivity(),
                        1
                    )
                    binding.orders.itemAnimator = DefaultItemAnimator()
                    binding.orders.adapter = myRecyclerViewAdapter
                } else {
                    val myRecyclerViewAdapter = OrderListRecyclerViewAdapter(
                        invoice
                    )
                    binding.orders.layoutManager = GridLayoutManager(
                        requireActivity(),
                        1
                    )
                    binding.orders.itemAnimator = DefaultItemAnimator()
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
            0 -> binding.title.text = "Recent Order"
            1 -> binding.title.text = "Pending Order"
        }
        setProgressDialog(requireActivity())
        lifecycleScope.launch {
            try {
                val response = viewModel.getOrderList(
                    PersistentUser.getInstance().getAccessToken(
                        requireActivity()
                    ), order
                )
                invoice = response.response
                if (response.response.isNotEmpty()) {
                    val myRecyclerViewAdapter = OrderListRecyclerViewAdapter(
                        response.response
                    )
                    binding.orders.layoutManager = GridLayoutManager(
                        requireActivity(),
                        1
                    )
                    binding.orders.itemAnimator = DefaultItemAnimator()
                    binding.orders.adapter = myRecyclerViewAdapter
                } else {
                    binding.orders.visibility = View.GONE
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
            "Current Order",
            "Pending Order",
            "Unpaid Order",
            "Delivered Order",
            "Cancelled Order"
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

        idWheelMenuCenterItem.setOnClickListener {
            if (currentItemPosition == selectedItemPosition){
                orderDialog.dismiss()
            } else {
                orderDialog.dismiss()
                currentItemPosition = selectedItemPosition
                getOrderList(selectedItemPosition)
            }
        }

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