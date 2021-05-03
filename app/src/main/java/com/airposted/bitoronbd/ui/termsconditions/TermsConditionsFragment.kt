package com.airposted.bitoronbd.ui.termsconditions

import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airposted.bitoronbd.databinding.FragmentTermsConditionsBinding
import com.airposted.bitoronbd.utils.AppHelper

class TermsConditionsFragment : Fragment() {
    private lateinit var binding: FragmentTermsConditionsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTermsConditionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        binding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.toolbar.toolbarTitle.text = "Terms & Conditions"
        val value = AppHelper.termsConditions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.content.text = Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT)
        } else {
            binding.content.text = Html.fromHtml(value)
        }
    }
}