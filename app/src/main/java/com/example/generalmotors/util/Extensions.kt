package com.example.generalmotors.util

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat

fun Context.hasManifestPermission(permissionType: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permissionType) == PackageManager.PERMISSION_GRANTED
}

fun BluetoothGattCharacteristic.isWritable() = this.properties and (BluetoothGattCharacteristic.PROPERTY_WRITE) != 0
fun BluetoothGattCharacteristic.isWritableWithoutResponse() = this.properties and (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0

