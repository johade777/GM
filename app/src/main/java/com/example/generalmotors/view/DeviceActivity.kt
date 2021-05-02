package com.example.generalmotors.view

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import com.example.generalmotors.R
import com.example.generalmotors.data.BluetoothDevices
import com.example.generalmotors.service.BLEService
import com.google.android.material.appbar.CollapsingToolbarLayout

class DeviceActivity : AppCompatActivity() {
    private lateinit var connectButton: Button
    private var connectedDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        setSupportActionBar(findViewById(R.id.detail_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val deviceId = intent.getStringExtra(ItemDetailFragment.ARG_ITEM_ID)
        val device = BluetoothDevices.ITEM_MAP[deviceId!!]

        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = BLEService.getDeviceName(BLEService.getDeviceName(device!!.name))
        connectButton = findViewById(R.id.connect_button)

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
            Toast.makeText(this, "Successfully connected to ${BLEService.getDeviceName(connectedDevice!!.name)}", Toast.LENGTH_SHORT).show()
            connectButton.text = "Disconnect"
        }
        connectButton.setOnClickListener {
            BLEService.closeBlueConnection()
        }
    }

    private fun disconnected(){
        runOnUiThread {
            Toast.makeText(this, "Successfully disconnected from ${BLEService.getDeviceName(connectedDevice!!.name)}", Toast.LENGTH_SHORT).show()
            connectedDevice = null
            connectButton.text = "Connect"
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