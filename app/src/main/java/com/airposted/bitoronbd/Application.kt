package com.airposted.bitoronbd

import android.app.Application
import com.airposted.bitoronbd.data.network.MyApi
import com.airposted.bitoronbd.data.network.NetworkConnectionInterceptor
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.data.repositories.*
import com.airposted.bitoronbd.ui.auth.AuthViewModelFactory
import com.airposted.bitoronbd.ui.home.HomeViewModelFactory
import com.airposted.bitoronbd.ui.location_set.LocationSetViewModelFactory
import com.airposted.bitoronbd.ui.main.MainViewModelFactory
import com.airposted.bitoronbd.ui.more.MoreViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class Application : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@Application))

        bind() from singleton { NetworkConnectionInterceptor(instance()) }
        bind() from singleton { MyApi(instance()) }
//        bind() from singleton { AppDatabase(instance()) }
        bind() from singleton { PreferenceProvider(instance()) }
        bind() from singleton { UserRepository(instance()) }
        bind() from singleton { HomeRepository(instance(), MyApi(instance())) }
        bind() from singleton { MoreRepository(instance(), MyApi(instance())) }
        bind() from singleton { MainRepository(instance()) }
        bind() from singleton { LocationSetRepository(instance()) }
        bind() from provider { AuthViewModelFactory(instance()) }
        bind() from provider { MoreViewModelFactory(instance()) }
        bind() from provider { MainViewModelFactory(instance()) }
        bind() from provider { HomeViewModelFactory(instance()) }
        bind() from provider { LocationSetViewModelFactory(instance()) }


    }

}