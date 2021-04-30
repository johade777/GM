package com.example.generalmotors.view

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.widget.NestedScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.generalmotors.adapter.BluetoothDeviceRecyclerAdapter
import com.example.generalmotors.R
import com.example.generalmotors.data.BluetoothDevices
import com.example.generalmotors.viewmodel.DeviceListViewModel

const val BLE_ENABLE_REQUEST = 1
const val FINE_LOCATION_REQUEST = 2

class ItemListActivity : AppCompatActivity() {

    private var twoPane: Boolean = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var scanButton: Button
    private lateinit var loadingIcon: ProgressBar
    private lateinit var viewModel: DeviceListViewModel
    private lateinit var adapter: BluetoothDeviceRecyclerAdapter
    private var blePermissionDenied = false
    private var isScanning = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        viewModel = ViewModelProvider(this, defaultViewModelProviderFactory).get(DeviceListViewModel::class.java)

        viewModel.isScanning.observe(this, {
            isScanning = it
            loadingIcon.isVisible = it
        })

        viewModel.scanText.observe(this, {
            scanButton.text = it
        })

        loadingIcon = findViewById(R.id.indeterminateBar)
        scanButton = findViewById(R.id.scan_button)
        scanButton.setOnClickListener {
            if(isScanning){
                viewModel.stopScan()
            }else{
                viewModel.startScan(this)
            }
        }

        if (findViewById<NestedScrollView>(R.id.item_detail_container) != null) {
            twoPane = true
        }

        recyclerView = findViewById(R.id.item_list)
        adapter = BluetoothDeviceRecyclerAdapter(this, BluetoothDevices.ITEMS, twoPane)
        recyclerView.adapter = adapter

        viewModel.getDeviceObservable().subscribe{
                var temp = it
                adapter.addDevice(temp)
        }
    }

    override fun onResume() {
        super.onResume()
        if(!viewModel.bluetoothEnabled() && !blePermissionDenied){
            viewModel.promptBLEPermission(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            BLE_ENABLE_REQUEST -> {
                if(requestCode != Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth Permission Required", Toast.LENGTH_SHORT).show()
                    blePermissionDenied = true
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            FINE_LOCATION_REQUEST -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    viewModel.requestLocationPermission(this)
                } else {
                    viewModel.startScan(this)
                }
            }
        }
    }
}