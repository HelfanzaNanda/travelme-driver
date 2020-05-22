package com.travelme.driver.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.travelme.driver.R
import com.travelme.driver.fragments.HomeFragment
import com.travelme.driver.fragments.MapsFragment
import com.travelme.driver.fragments.ProfileFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object{ var navStatus = -1 }
    private var fragment : Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        if(savedInstanceState == null){
            navigation.selectedItemId = R.id.navigation_home
        }
        supportActionBar?.hide()
        //isLoggedIn()
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId){
            R.id.navigation_home -> {
                if(navStatus != 0){
                    fragment = HomeFragment()
                    navStatus = 0
                }
            }
            /*R.id.navigation_maps -> {
                if(navStatus != 1){
                    fragment = MapsFragment()
                    navStatus = 1
                }
            }*/
            R.id.navigation_profile -> {
                if(navStatus != 2){
                    fragment = ProfileFragment()
                    navStatus = 2
                }
            }
        }
        if(fragment == null){
            navStatus = 0
            fragment = HomeFragment()
        }

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.screen_container, fragment!!)
        fragmentTransaction.commit()
        true
    }

    /*private fun isLoggedIn(){
        if(Constants.getToken(this@MainActivity).equals("UNDEFINED")){
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }).also { finish() }
        }
    }*/

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
