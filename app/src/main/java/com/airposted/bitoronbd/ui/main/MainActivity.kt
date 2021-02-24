package com.airposted.bitoronbd.ui.main

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.ActivityMainBinding
import com.airposted.bitoronbd.ui.home.HomeFragment
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class MainActivity : AppCompatActivity(), KodeinAware, CommunicatorFragmentInterface {
    private lateinit var mainBinding: ActivityMainBinding
    override val kodein by kodein()

    private val factory: MainViewModelFactory by instance()
    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel
    private var active = HomeFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.productFragment || destination.id == R.id.locationSetFragment || destination.id == R.id.receiverAddressFragment) {
                mainBinding.bottomNavigation.visibility = View.GONE
            } else {
                mainBinding.bottomNavigation.visibility = View.VISIBLE
            }
        }

        mainBinding.bottomNavigation.setupWithNavController(navController)

        mainBinding.bottomNavigation.setOnNavigationItemReselectedListener {

        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_navigation, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun addContentFragment(fragment: Fragment?, addToBackStack: Boolean) {
        mainBinding.bottomNavigation.visibility = View.GONE
        if (fragment == null) {
            return
        }
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.nav_host_fragment)

        if (currentFragment != null && fragment.javaClass.isAssignableFrom(currentFragment.javaClass)) {
            return
        }

        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.nav_host_fragment, fragment, fragment.javaClass.name)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.javaClass.name)
        }
        fragmentTransaction.commit()
        fragmentManager.executePendingTransactions()
    }

    override fun onBackPressed() {
        Log.e("aaaaaa", supportFragmentManager.backStackEntryCount.toString())
        super.onBackPressed()
        if (supportFragmentManager.backStackEntryCount == 0) {
            mainBinding.bottomNavigation.visibility = View.VISIBLE
        }
    }
}