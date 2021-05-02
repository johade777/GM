package com.example.generalmotors.service

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.generalmotors.util.hasManifestPermission
import com.example.generalmotors.util.isWritable
import com.example.generalmotors.util.isWritableWithoutResponse
import com.example.generalmotors.util.printGattTable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.*

const val BLE_ENABLE_REQUEST = 1
const val FINE_LOCATION_REQUEST = 2

const val CLASSIC_DEVICE = 1
const val BLE_DEVICE = 2
const val DUAL_DEVICE = 3

//Assumption
const val EP1_UNLOCK_SERVICE_UUID = "00001800-0000-1000-8000-00805f9b34fb"
const val EP1_UNLOCK_CHARACTERISTIC_UUID = "00002a00-0000-1000-8000-00805f9b34fb"

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
                if(!foundBluetoothDevices.contains(this) && this.type == BLE_DEVICE){
                    foundBluetoothDevices.add(this)
                    foundDeviceObservable.onNext(this)
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
           requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,
                   FINE_LOCATION_REQUEST,
                   activity
            )
        }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    private fun requestPermission(permission: String, requestCode: Int, activity: Activity) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
    }

    companion object {
        var connectionStatus: BehaviorSubject<Int> = BehaviorSubject.create()
        private var connectedDevice: BluetoothDevice? = null
        private var connectedGatt: BluetoothGatt? = null
        private var deviceCharacteristics = mutableMapOf<String, BluetoothGattCharacteristic>()
        private var deviceServices = mutableMapOf<String, BluetoothGattService>()

        fun getDeviceType(deviceTypeInteger: Int): String {
            return when (deviceTypeInteger) {
                CLASSIC_DEVICE -> "Classic Device"
                BLE_DEVICE -> "Low Energy Device"
                DUAL_DEVICE -> "Dual Device"
                else -> "Unknown Device Type"
            }
        }

        fun getDeviceName(name: String?) : String{
            return if (name == "" || name == null) "N/A" else name
        }

        private fun setConnectedDevice(device: BluetoothDevice){
            connectedDevice = device
        }

        fun getConnectedDevice(): BluetoothDevice? {
            return connectedDevice
        }

        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                val deviceAddress = gatt.device.address
                connectedGatt = gatt

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                        setConnectedDevice(gatt.device)
                        gatt.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                        gatt.close()
                    }
                } else {
                    Log.w("BluetoothGattCallback", "Error $status encountered for  deviceAddress! Disconnecting...")
                    gatt.close()
                    connectionStatus.onNext(-1)
                }

                connectionStatus.onNext(newState)
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if(gatt != null){
                    val discoveredServices = mutableMapOf<String, BluetoothGattService>()
                    for(service in gatt.services){
                        if(!discoveredServices.containsKey(service.uuid.toString())) {
                            discoveredServices[service.uuid.toString()] = service
                        }
                    }
                    deviceServices = discoveredServices
                }
            }

            override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                super.onCharacteristicWrite(gatt, characteristic, status)
            }
        }

        private fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
            val writeType = when {
                characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                characteristic.isWritableWithoutResponse() -> {
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                }
                else -> error("Characteristic ${characteristic.uuid} cannot be written to")
            }

            connectedGatt?.let { gatt ->
                characteristic.writeType = writeType
                characteristic.value = payload
                gatt.writeCharacteristic(characteristic)
            } ?: error("Not connected to a BLE device!")
        }

        fun closeBlueConnection(){
            if (connectedGatt != null) {
                connectedGatt!!.disconnect()
            }
        }

        fun unlockEP1(){
            val unlockCharacteristicUUID = UUID.fromString(EP1_UNLOCK_CHARACTERISTIC_UUID)
            val unlockCharacteristic = deviceServices[EP1_UNLOCK_SERVICE_UUID]?.getCharacteristic(unlockCharacteristicUUID)
            if(unlockCharacteristic != null) {
                writeCharacteristic(unlockCharacteristic, byteArrayOf(1))
            }
        }
    }
}