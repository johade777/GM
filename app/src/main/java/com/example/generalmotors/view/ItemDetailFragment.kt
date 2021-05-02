package com.example.generalmotors.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.generalmotors.R
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import android.util.Log
import android.widget.Toast
import com.example.generalmotors.data.BluetoothDevices
import com.example.generalmotors.service.BLEService

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListActivity]
 * in two-pane mode (on tablets) or a [ItemDetailActivity]
 * on handsets.
 */
class ItemDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: BluetoothDevice? = null
    private lateinit var connectButton: Button
    private var connectedDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                item = BluetoothDevices.ITEM_MAP[it.getString(ARG_ITEM_ID)]
                activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = BLEService.getDeviceName(item?.name)
            }
        }
        BLEService.connectionStatus.subscribe(){
                if (it == BluetoothProfile.STATE_CONNECTED) {
                    connectedDevice = BLEService.getConnectedDevice()
                    connected()
                } else if (it == BluetoothProfile.STATE_DISCONNECTED) {
                    disconnected()
                    connectedDevice = null
                }
        }
    }

    private fun connected(){
        activity!!.runOnUiThread {
            Toast.makeText(context, "Successfully connected to ${BLEService.getDeviceName(connectedDevice!!.name)}", Toast.LENGTH_SHORT).show()
            connectButton.text = "Disconnect"
        }
        connectButton.setOnClickListener {
//            BLEService.gattCallback.
        }
    }

    private fun disconnected(){
        activity!!.runOnUiThread {
            Toast.makeText(context, "Successfully disconnected to ${BLEService.getDeviceName(connectedDevice!!.name)}", Toast.LENGTH_SHORT).show()
            connectButton.text = "Connect"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.item_detail, container, false)

        item?.let {
            var device = it
            rootView.findViewById<TextView>(R.id.item_detail).text = BLEService.getDeviceType(device.type)
            connectButton = rootView.findViewById(R.id.connect_button)
            connectButton.setOnClickListener {
                device.connectGatt(context, false, BLEService.gattCallback)
            }
        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }
}