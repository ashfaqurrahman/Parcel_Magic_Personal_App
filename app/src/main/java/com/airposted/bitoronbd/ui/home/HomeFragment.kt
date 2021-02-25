package com.airposted.bitoronbd.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentHomeBinding
import com.airposted.bitoronbd.ui.location_set.LocationSetFragment
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.ui.product.ProductFragment
import com.airposted.bitoronbd.utils.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class HomeFragment : Fragment(R.layout.fragment_home), KodeinAware {

    private lateinit var homeBinding: FragmentHomeBinding
    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()
    private lateinit var viewModel: HomeViewModel
    var myCommunicator: CommunicatorFragmentInterface? = null

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

        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).menu.getItem(0).isChecked = true
        myCommunicator = context as CommunicatorFragmentInterface

        //setProgressDialog(requireActivity())
        lifecycleScope.launch {
            try {
                val settingResponse = viewModel.getSetting()
                PreferenceProvider(requireActivity()).saveSharedPreferences("rate", settingResponse.rate.perKmPrice.toString())
                //dismissDialog()
            } catch (e: ApiException) {
                //dismissDialog()
                homeBinding.rootLayout.snackbar(e.message!!)
                e.printStackTrace()
            } catch (e: NoInternetException) {
                //dismissDialog()
                homeBinding.rootLayout.snackbar(e.message!!)
                e.printStackTrace()
            }
        }

        homeBinding.address.text = PreferenceProvider(requireActivity()).getSharedPreferences("currentLocation")

        homeBinding.address.setOnClickListener {
            myCommunicator?.addContentFragment(LocationSetFragment(), true)
            //findNavController().navigate(R.id.action_homeFragment_to_locationSetFragment)
        }

        homeBinding.productBtn.setOnClickListener{
            myCommunicator?.addContentFragment(ProductFragment(), true)
            //findNavController().navigate(R.id.action_homeFragment_to_productFragment)
        }
    }
}