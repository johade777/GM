package com.example.generalmotors.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.generalmotors.data.BluetoothDevice
import com.example.generalmotors.services.BLEService
import io.reactivex.Observable

class DeviceListViewModel(application: Application) : AndroidViewModel(application) {
    private val bluetoothService = BLEService(application.applicationContext)

    var isScanning: LiveData<Boolean> = bluetoothService.getScanningLiveData()
    var scanText: MutableLiveData<String> = MutableLiveData("Scan")

    init{
        getDeviceObservable()
    }

    fun promptBLEPermission(activity: Activity){
        bluetoothService.promptBLEPermission(activity)
    }

    fun requestLocationPermission(activity: Activity){
        bluetoothService.requestLocationPermission(activity)
    }

    private fun getDeviceObservable(){
        bluetoothService.deviceChangedObservable().subscribe{ it ->
            var temp = it
            var wait = "wait"
        }
    }

    fun startScan(activity: Activity){
        bluetoothService.startBleScan(activity)
        scanText.value = "Stop Scan"
    }

    fun stopScan(){
        bluetoothService.stopScan()
        scanText.value = "Scan"
    }

    fun bluetoothEnabled() : Boolean {
        return bluetoothService.isEnabled()
    }
}