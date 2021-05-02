package com.example.generalmotors.view

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.generalmotors.R
import com.example.generalmotors.data.BluetoothDevices
import com.example.generalmotors.service.BLEService
import com.google.android.material.appbar.CollapsingToolbarLayout

class DeviceActivity : AppCompatActivity() {
    private lateinit var connectButton: Button
    private lateinit var unlockButton: Button
    private var connectedDevice: BluetoothDevice? = null
    private lateinit var deviceId: String
    private lateinit var device: BluetoothDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        setSupportActionBar(findViewById(R.id.detail_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        deviceId = intent.getStringExtra(ItemDetailFragment.ARG_ITEM_ID)!!
        device = BluetoothDevices.ITEM_MAP[deviceId]!!

        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = BLEService.getDeviceName(BLEService.getDeviceName(device.name))
        findViewById<TextView>(R.id.item_detail).text = BLEService.getDeviceType(device.type)
        connectButton = findViewById(R.id.connect_button)
        unlockButton = findViewById(R.id.unlock_button)

        unlockButton.setOnClickListener {
            BLEService.unlockEP1()
        }

        connectButton.setOnClickListener {
            device.connectGatt(this, false, BLEService.gattCallback)
        }

        BLEService.connectionStatus.subscribe(){
            if (it == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = BLEService.getConnectedDevice()
                connected()
            } else if (it == BluetoothProfile.STATE_DISCONNECTED) {
                disconnected()
            }
        }
    }

    private fun connected(){
        runOnUiThread {
            Toast.makeText(this, "Successfully connected to ${BLEService.getDeviceName(connectedDevice?.name)}", Toast.LENGTH_SHORT).show()
            connectButton.text = "Disconnect"
            unlockButton.visibility = View.VISIBLE
        }
        connectButton.setOnClickListener {
            BLEService.closeBlueConnection()
        }
    }

    private fun disconnected(){
        runOnUiThread {
            Toast.makeText(this, "Successfully disconnected from ${BLEService.getDeviceName(connectedDevice?.name)}", Toast.LENGTH_SHORT).show()
            connectedDevice = null
            connectButton.text = "Connect"
            unlockButton.visibility = View.GONE
        }
        connectButton.setOnClickListener {
            device?.connectGatt(this, false, BLEService.gattCallback)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                navigateUpTo(Intent(this, ItemListActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}