package com.taeprog.qrreader.utils

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class PermissionsManager(
    private val context: AppCompatActivity,
    permissions: List<PermissionItem>?
) {
    var callback: ()->Unit = {}
    var awaiting = false
    val permissions = mutableListOf<PermissionItem>()

    fun permissionGranted(): Boolean {
        var ret = true
        for (i in permissions.indices) {
            ret = ret && (ActivityCompat.checkSelfPermission(context, permissions[i].permission)
                    == PackageManager.PERMISSION_GRANTED)
        }
        return ret
    }

    private fun showAlert(permissionItem: PermissionItem, requestCode: Int, settings: Boolean) {
        val ad = AlertDialog.Builder(
            context
        )
        val message =
            permissionItem.message + if (settings) "\nПожалуйста перейдите в настройки и дайте разрешение." else "\nЗапросить ещё раз?"
        ad.setTitle(permissionItem.title)
        ad.setMessage(message)
        if (settings) {
            ad.setPositiveButton("Перейти в настройки") { dialog: DialogInterface?, which: Int ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivityForResult(intent, 22)
            }
        } else {
            ad.setPositiveButton("Да") { dialog: DialogInterface?, which: Int ->
                ActivityCompat.requestPermissions(
                    context, arrayOf(permissionItem.permission), requestCode
                )
            }
        }
        ad.setNegativeButton("Закрыть приложение") { dialog: DialogInterface?, which: Int ->
            System.exit(
                0
            )
        }
        ad.show()
    }

    fun requestPermissions() {
        if (awaiting) return
        for (i in permissions.indices) {
            if (ActivityCompat.checkSelfPermission(context, permissions[i].permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (!permissions[i].requested) {
                    awaiting = true
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(permissions[i].permission),
                        i
                    )
                    permissions[i].requested = true
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        permissions[i].permission
                    )
                ) {
                    awaiting = true
                    showAlert(permissions[i], i, false)
                } else {
                    showAlert(permissions[i], i, true)
                }
            }
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>?,
        grantResults: IntArray
    ) {
        awaiting = false
        if (grantResults.size > 0) {
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions()
                }
            }
        }
        if (permissionGranted()) {
            callback()
        }
    }


    class PermissionItem(var title: String, var message: String, var permission: String) {
        var requested = false
    }

    init {
        this.permissions.addAll(permissions!!)
    }
}