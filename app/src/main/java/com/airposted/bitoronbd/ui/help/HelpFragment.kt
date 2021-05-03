package com.airposted.bitoronbd.ui.help

import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentHelpBinding
import com.airposted.bitoronbd.utils.AppHelper

class HelpFragment : Fragment() {
    private lateinit var binding: FragmentHelpBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHelpBinding.inflate(inflater, container, false)
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
        binding.toolbar.toolbarTitle.text = getString(R.string.help)
        val value = AppHelper.help
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.content.text = Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT)
        } else {
            binding.content.text = Html.fromHtml(value)
        }
    }
}
