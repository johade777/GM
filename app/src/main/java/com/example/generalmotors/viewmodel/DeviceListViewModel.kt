package com.example.generalmotors.viewmodel

import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.generalmotors.service.BLEService
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

    fun getDeviceObservable(): Observable<BluetoothDevice>{
        return bluetoothService.deviceChangedObservable()
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