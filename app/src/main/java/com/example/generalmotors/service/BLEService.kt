package com.example.generalmotors.service

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.generalmotors.data.BluetoothDevice
import com.example.generalmotors.util.hasManifestPermission
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.ArrayList

const val BLE_ENABLE_REQUEST = 1
const val FINE_LOCATION_REQUEST = 2

const val CLASSIC_DEVICE = 1
const val BLE_DEVICE = 2
const val DUAL_DEVICE = 3

class BLEService(context: Context) {

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val scanner = bluetoothAdapter.bluetoothLeScanner

    private var isFineLocationGranted: Boolean = context.hasManifestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    private val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build()
    private var isScanning : MutableLiveData<Boolean> = MutableLiveData()
    private var foundDeviceObservable: BehaviorSubject<BluetoothDevice> = BehaviorSubject.create()
    private val foundBluetoothDevices: MutableList<BluetoothDevice> = ArrayList()

    init{
        isScanning.value = false
    }

    fun startBleScan(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isFineLocationGranted) {
            requestLocationPermission(activity)
        }
        else {
            if(scanner != null) {
                scanner.startScan(null, scanSettings, scanCallback)
                isScanning.value = true
            }
        }
    }

    fun isEnabled() : Boolean {
        return bluetoothAdapter.isEnabled
    }

    fun stopScan(){
        scanner.stopScan(scanCallback)
        isScanning.value = false
    }

    fun deviceChangedObservable(): Observable<BluetoothDevice>{
        return foundDeviceObservable
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                val foundName = if (name == null) "N/A" else name
                val foundDevice = BluetoothDevice(
                    foundName,
                    address,
                    address,
                    result.device.type
                )
                if(!foundBluetoothDevices.contains(foundDevice) && foundDevice.deviceType == BLE_DEVICE){
                    foundBluetoothDevices.add(foundDevice)
                    foundDeviceObservable.onNext(foundDevice)
                }
            }
        }
    }

    fun getScanningLiveData(): LiveData<Boolean> {
        return isScanning
    }

    fun requestLocationPermission(activity: Activity) {
        if (isFineLocationGranted) {
            return
        }

        activity.runOnUiThread {
            showAlertDialog(activity)
        }
    }

    fun promptBLEPermission(activity: Activity){
        if(!bluetoothAdapter.isEnabled){
            val bleIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(bleIntent, BLE_ENABLE_REQUEST)
        }
    }

    private fun showAlertDialog(activity: Activity) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialog.setTitle("AlertDialog")
        alertDialog.setMessage("Do you wanna close this Dialog?")
        alertDialog.setPositiveButton("yes") { _, _ ->
            activity.requestPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                FINE_LOCATION_REQUEST
            )
        }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    companion object {
        fun GetDeviceType(deviceTypeInteger: Int): String {
            return when (deviceTypeInteger) {
                CLASSIC_DEVICE -> "Classic Device"
                BLE_DEVICE -> "Low Energy Device"
                DUAL_DEVICE -> "Dual Device"
                else -> "Unknown Device Type"
            }
        }
    }
}