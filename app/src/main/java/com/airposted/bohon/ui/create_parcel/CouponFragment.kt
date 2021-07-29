package com.airposted.bohon.ui.create_parcel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.airposted.bohon.R
import com.airposted.bohon.databinding.FragmentCouponBinding
import com.airposted.bohon.databinding.FragmentQuantityBinding
import com.airposted.bohon.ui.location_set.LocationSetViewModel
import com.airposted.bohon.ui.location_set.LocationSetViewModelFactory
import com.airposted.bohon.ui.main.CommunicatorFragmentInterface
import com.airposted.bohon.utils.*
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class CouponFragment : Fragment(), KodeinAware {
    private lateinit var binding: FragmentCouponBinding
    override val kodein by kodein()
    private val factory: LocationSetViewModelFactory by instance()
    private lateinit var viewModel: LocationSetViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCouponBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, factory).get(LocationSetViewModel::class.java)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        binding.toolbar.toolbarTitle.text = "Coupon Code"
        binding.apply.setOnClickListener {
            if (binding.couponText.text.toString().isNotEmpty()) {
                setProgressDialog(requireContext())
                lifecycleScope.launch {
                    try {
                        hideKeyboard(requireActivity())
                        val response = viewModel.checkCoupon(binding.couponText.text.toString())
                        if (response.message == "Successful") {
                            dismissDialog()
                            requireActivity().onBackPressed()
                        } else {
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

        binding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
            hideKeyboard(requireActivity())
        }
    }

}