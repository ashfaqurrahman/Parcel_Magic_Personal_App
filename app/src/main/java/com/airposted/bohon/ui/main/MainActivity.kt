package com.airposted.bohon.ui.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.airposted.bohon.R
import com.airposted.bohon.databinding.ActivityMainBinding
import com.airposted.bohon.ui.home.HomeFragment
import com.airposted.bohon.ui.my_order.MyParcelFragment
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein


class MainActivity : AppCompatActivity(), KodeinAware, CommunicatorFragmentInterface {
    private lateinit var mainBinding: ActivityMainBinding
    override val kodein by kodein()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val bundle = intent.extras
        if (bundle != null) {
            val data = bundle.get("category")
            Log.e("aaaaaa", data.toString())
            //bundle must contain all info sent in "data" field of the notification
        }

        if (intent.getStringExtra("notification") != null){
            addContentFragment(MyParcelFragment(), true)
        } else {
            addContentFragment(HomeFragment(), false)
        }

    }

    override fun setContentFragment(fragment: Fragment?, addToBackStack: Boolean) {
        if (fragment == null) {
            return
        }
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.frameLayout)
        if (currentFragment != null && fragment.javaClass.isAssignableFrom(currentFragment.javaClass)) {
            return
        }
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment, fragment.javaClass.name)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.javaClass.name)
        }
        fragmentTransaction.commit()
        fragmentManager.executePendingTransactions()
    }

    override fun addContentFragment(fragment: Fragment?, addToBackStack: Boolean) {
        if (fragment == null) {
            return
        }
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.frameLayout)

        if (currentFragment != null && fragment.javaClass.isAssignableFrom(currentFragment.javaClass)) {
            return
        }

        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_up, 0, 0, R.anim.slide_in_down)
        fragmentTransaction.add(R.id.frameLayout, fragment, fragment.javaClass.name)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.javaClass.name)
        }
        fragmentTransaction.commit()
        fragmentManager.executePendingTransactions()
    }
}