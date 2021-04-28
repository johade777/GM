package com.example.generalmotors.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun Context.hasManifestPermission(permissionType: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permissionType) == PackageManager.PERMISSION_GRANTED
}