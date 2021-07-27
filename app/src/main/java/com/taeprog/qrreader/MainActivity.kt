package com.taeprog.qrreader

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.taeprog.qrreader.databinding.ActivityMainBinding
import com.taeprog.qrreader.utils.PermissionsManager

class MainActivity : AppCompatActivity() {

    lateinit var permissionsManager:PermissionsManager

    lateinit var binding:ActivityMainBinding
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionsManager = PermissionsManager(this, listOf(
            PermissionsManager.PermissionItem(
                getString(R.string.camera_permission_title),
                getString(R.string.camera_permission_message),
                Manifest.permission.CAMERA
            )
        ))


        if(!permissionsManager.permissionGranted()){
            permissionsManager.requestPermissions()
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onResume() {
        super.onResume()
        if(!permissionsManager.permissionGranted()){
            permissionsManager.requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(!permissionsManager.permissionGranted()){
            permissionsManager.requestPermissions()
        }
    }
}