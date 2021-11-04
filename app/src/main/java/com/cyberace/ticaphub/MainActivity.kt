package com.cyberace.ticaphub

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

//const val baseUrl = "http://10.0.2.2/ticap/"
const val baseUrl = "https://www.ticaphub.com/api/"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_user_account))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    var isDoublePressed = false
    override fun onBackPressed() {
//        val sft = supportFragmentManager.beginTransaction()
//        if  (supportFragmentManager.fragments.isNotEmpty()){
//            sft.addToBackStack(null)
//            sft.commit()
//        }

//        if (supportFragmentManager.backStackEntryCount == 0) {
//            this.finish();
//        } else {
//            super.onBackPressed();
//        }

        //TODO FIX ONBACK KEY PARA HINDI SIYA WHITE SCREEN
        //TODO FIX ALSO NA AFTER LOGIN, HINDI NA PWEDE MAGBACK SA LOGIN SCREEN SI USER, this.finish ata yun

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        if(navView.selectedItemId == R.id.navigation_home){
            this.finish()
        }else{
            super.onBackPressed()
        }
    }
}

