package com.airposted.bitoronbd.ui.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentHomeBinding
import com.airposted.bitoronbd.ui.location_set.LocationSetFragment
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.ui.my_parcel.MyParcelFragment
import com.airposted.bitoronbd.ui.product.ProductFragment
import com.airposted.bitoronbd.ui.product.ReceiverInfoFragment
import com.airposted.bitoronbd.utils.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class HomeFragment : Fragment(R.layout.fragment_home),
    NavigationView.OnNavigationItemSelectedListener, KodeinAware {

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

        /*val wp: WindowManager.LayoutParams = requireActivity().window.attributes
        wp.dimAmount = 0.75f*/

        homeBinding.menu.setOnClickListener {
            homeBinding.drawerLayout.openDrawer(Gravity.LEFT)
        }

        homeBinding.navigationView.setNavigationItemSelectedListener(this)

        myCommunicator = context as CommunicatorFragmentInterface

        //setProgressDialog(requireActivity())
        lifecycleScope.launch {
            try {
                val settingResponse = viewModel.getSetting()
                PreferenceProvider(requireActivity()).saveSharedPreferences(
                    "rate_quick",
                    settingResponse.rate.perKmPrice.toString()
                )
                PreferenceProvider(requireActivity()).saveSharedPreferences(
                    "rate_express",
                    settingResponse.rate.perKmPrice.toString()
                )
                PreferenceProvider(requireActivity()).saveSharedPreferences(
                    "base_price_quick",
                    settingResponse.rate.basePriceQuick.toString()
                )
                PreferenceProvider(requireActivity()).saveSharedPreferences(
                    "base_price_express",
                    settingResponse.rate.basePriceExpress.toString()
                )
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

//        homeBinding.address.text = PreferenceProvider(requireActivity()).getSharedPreferences("currentLocation")

        homeBinding.address.setOnClickListener {
            myCommunicator?.addContentFragment(LocationSetFragment(), true)
        }

        homeBinding.expressBtn.setOnClickListener{
            myCommunicator?.addContentFragment(ReceiverInfoFragment(), true)
            //findNavController().navigate(R.id.action_homeFragment_to_productFragment)
        }

        homeBinding.whatToSend.setOnClickListener {
            openWhatToSendDialog()
        }

        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                ContextCompat.getColor(requireActivity(), R.color.color1),
                ContextCompat.getColor(requireActivity(), R.color.color2),
                ContextCompat.getColor(requireActivity(), R.color.color3),
                ContextCompat.getColor(requireActivity(), R.color.color4)
            )
        )

        homeBinding.expressBtn.background = gradientDrawable
        homeBinding.quickBtn.background = gradientDrawable
    }

    private fun openWhatToSendDialog() {
        val orderDialog = Dialog(requireContext())
        orderDialog.setContentView(R.layout.what_to_send_dialog)
        orderDialog.findViewById<TextView>(R.id.got_it).setOnClickListener {
            orderDialog.dismiss()
        }
        orderDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        orderDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        orderDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation_2
        orderDialog.window?.attributes?.gravity = Gravity.BOTTOM
        orderDialog.setCancelable(true)
        orderDialog.show()
    }

    private fun productTypeDialog(){
        val dialogs = Dialog(requireActivity())
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(R.layout.product_type)
        dialogs.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogs.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  //w
            ViewGroup.LayoutParams.WRAP_CONTENT //h
        )

        val close = dialogs.findViewById<ImageView>(R.id.close)
        val fragile = dialogs.findViewById<CardView>(R.id.fragile)
        val liquid = dialogs.findViewById<CardView>(R.id.liquid)
        val solid = dialogs.findViewById<CardView>(R.id.solid)
        val fragileSelect = dialogs.findViewById<RelativeLayout>(R.id.fragile_select)
        val liquidSelect = dialogs.findViewById<RelativeLayout>(R.id.liquid_select)
        val solidSelect = dialogs.findViewById<RelativeLayout>(R.id.solid_select)
        val productDetailsLayout = dialogs.findViewById<TextInputLayout>(R.id.product_details_layout)
        val productDetails = dialogs.findViewById<EditText>(R.id.product_details)
        val nextLayout = dialogs.findViewById<LinearLayout>(R.id.next_layout)
        val proceed = dialogs.findViewById<TextView>(R.id.proceed)

        close.setOnClickListener {
            dialogs.dismiss()
        }

        fragile.setOnClickListener {
            fragileSelect.visibility = View.VISIBLE
            liquidSelect.visibility = View.GONE
            solidSelect.visibility = View.GONE
            productDetailsLayout.visibility = View.VISIBLE
            nextLayout.visibility = View.VISIBLE
        }

        liquid.setOnClickListener {
            fragileSelect.visibility = View.GONE
            liquidSelect.visibility = View.VISIBLE
            solidSelect.visibility = View.GONE
            productDetailsLayout.visibility = View.VISIBLE
            nextLayout.visibility = View.VISIBLE
        }

        solid.setOnClickListener {
            fragileSelect.visibility = View.GONE
            liquidSelect.visibility = View.GONE
            solidSelect.visibility = View.VISIBLE
            productDetailsLayout.visibility = View.VISIBLE
            nextLayout.visibility = View.VISIBLE
        }

        proceed.setOnClickListener {
            if (productDetails.text.toString().isNotEmpty()){
                dialogs.dismiss()
                hideKeyboard(requireActivity())
                myCommunicator?.addContentFragment(ProductFragment(), true)
            } else {
                Toast.makeText(requireActivity(), "Please enter product details", Toast.LENGTH_LONG).show()
            }
        }

        dialogs.setCancelable(true)

        dialogs.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.my_parcel -> {
                myCommunicator?.addContentFragment(ProductFragment(), true)
                homeBinding.drawerLayout.closeDrawers()
            }
            R.id.parcel_history -> {
                myCommunicator?.addContentFragment(MyParcelFragment(), true)
                homeBinding.drawerLayout.closeDrawers()
            }
            R.id.profile -> {

                homeBinding.drawerLayout.closeDrawers()
            }
            R.id.help -> {

                homeBinding.drawerLayout.closeDrawers()
            }
            R.id.settings -> {

                homeBinding.drawerLayout.closeDrawers()
            }
            R.id.terms_condition -> {

                homeBinding.drawerLayout.closeDrawers()
            }
            R.id.sign_out -> {

                homeBinding.drawerLayout.closeDrawers()
            }
        }

        return true
    }
}