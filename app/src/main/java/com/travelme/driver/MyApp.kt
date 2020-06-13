package com.travelme.driver

import android.app.Application
import com.travelme.driver.activities.login_activity.LoginViewModel
import com.travelme.driver.fragments.home_fragment.OrderFragmentViewModel
import com.travelme.driver.fragments.maps_fragment.MapsViewModel
import com.travelme.driver.fragments.profile_fragment.ProfileViewModel
import com.travelme.driver.repositories.DriverRepository
import com.travelme.driver.repositories.OrderRepository
import com.travelme.driver.webservices.ApiClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class MyApp : Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApp)
            modules(listOf(repositoryModules, viewModelModules, retrofitModule))
        }
    }
}

val retrofitModule = module { single { ApiClient.instance() } }

val repositoryModules = module {
    factory { OrderRepository(get()) }
    factory { DriverRepository(get()) }
}

val viewModelModules = module {
    viewModel { OrderFragmentViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { MapsViewModel(get()) }
}