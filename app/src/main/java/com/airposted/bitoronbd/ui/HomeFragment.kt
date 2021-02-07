package com.airposted.bitoronbd.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentHomeBinding
import com.airposted.bitoronbd.ui.more.MoreViewModel
import com.airposted.bitoronbd.ui.more.MoreViewModelFactory
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class HomeFragment : Fragment(R.layout.fragment_home), KodeinAware {

    private lateinit var homeBinding: FragmentHomeBinding
    override val kodein by kodein()

    private val factory: MoreViewModelFactory by instance()
    private lateinit var viewModel: MoreViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)

        return homeBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        homeBinding.productBtn.setOnClickListener{
            Navigation.findNavController(requireView()).navigate(
                R.id.productFragment)
        }
    }
}