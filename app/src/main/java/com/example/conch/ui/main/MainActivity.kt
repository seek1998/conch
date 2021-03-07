package com.example.conch.ui.main

import android.Manifest
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.conch.R
import com.example.conch.utils.InjectUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.permissionx.guolindev.PermissionX
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var playCard: CardView

    private val viewModel by viewModels<MainViewModel> {
        InjectUtil.provideMainViewModelFactory(this)
    }

    private val TAG = this::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPermission()

        playCard = findViewById(R.id.main_card)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController.apply {
            addOnDestinationChangedListener { controller, destination, arguments ->
                playCard.visibility = if (destination.id == R.id.navigation_account) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        }

        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        navView.setupWithNavController(navController)

        viewModel.checkRecentPlay()

    }

    private fun initPermission() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.CALL_PHONE
            )
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    Log.i(TAG, "All permissions are granted")
                } else {
                    Log.i(TAG, "These permissions are denied: $deniedList")
                }
            }

    }

    var pressedTime = 0L

    override fun onBackPressed() {

        val nowTime = System.currentTimeMillis()
        if (nowTime - pressedTime > 2000) {
            Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT).show()
            pressedTime = nowTime
        } else {
            this.finish()
            exitProcess(0)
        }
    }

}