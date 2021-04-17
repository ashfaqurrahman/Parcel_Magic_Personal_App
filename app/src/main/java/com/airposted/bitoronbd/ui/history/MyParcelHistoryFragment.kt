package com.airposted.bitoronbd.ui.history

import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.MalformedJsonException
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.airposted.bitoronbd.databinding.FragmentMyParcelHistoryBinding
import com.airposted.bitoronbd.model.DataX
import com.airposted.bitoronbd.utils.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class MyParcelHistoryFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var binding: FragmentMyParcelHistoryBinding
    private val factory: MyParcelHistoryViewModelFactory by instance()
    private lateinit var viewModel: MyParcelHistoryViewModel
    private lateinit var invoice:List<DataX>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyParcelHistoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), factory).get(MyParcelHistoryViewModel::class.java)
        bindUI()
    }

    private fun bindUI() {
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        getOrderList(1)

        binding.searchItem.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
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
        }

        binding.searchItem.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if(!s.toString().contains("#")){
                    binding.searchItem.setText("#")
                    Selection.setSelection(binding.searchItem.text, binding.searchItem.text.length)

                }
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
                if(s.length > 1) {
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
                            val myRecyclerViewAdapter = OrderHistoryListRecyclerViewAdapter(
                                listNew
                            )
                            binding.orders.adapter = myRecyclerViewAdapter
                        }
                    } else {
                        binding.orders.visibility = View.VISIBLE
                        binding.noOrder.visibility = View.GONE
                        val myRecyclerViewAdapter = OrderHistoryListRecyclerViewAdapter(
                            invoice
                        )
                        binding.orders.adapter = myRecyclerViewAdapter
                    }
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
                    val myRecyclerViewAdapter = OrderHistoryListRecyclerViewAdapter(
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
}