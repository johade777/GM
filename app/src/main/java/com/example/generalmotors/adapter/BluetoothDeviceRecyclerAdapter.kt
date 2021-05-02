package com.example.generalmotors.adapter

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.generalmotors.R
import com.example.generalmotors.data.BluetoothDevices
import com.example.generalmotors.service.BLEService
import com.example.generalmotors.view.DeviceActivity
import com.example.generalmotors.view.ItemDetailActivity
import com.example.generalmotors.view.ItemDetailFragment
import com.example.generalmotors.view.ItemListActivity
import com.example.generalmotors.viewmodel.DeviceListViewModel

class BluetoothDeviceRecyclerAdapter(private val parentActivity: ItemListActivity, private var values: MutableList<BluetoothDevice>, private var viewModel: DeviceListViewModel) : RecyclerView.Adapter<BluetoothDeviceRecyclerAdapter.BluetoothDeviceViewHolder>() {

    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
        val item = v.tag as BluetoothDevice
            val intent = Intent(v.context, DeviceActivity::class.java).apply {
                putExtra(ItemDetailFragment.ARG_ITEM_ID, item.address)
            }
            viewModel.stopScan()
            v.context.startActivity(intent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothDeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_content, parent, false)
        return BluetoothDeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: BluetoothDeviceViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = BLEService.getDeviceName(item.name)
        holder.contentView.text = item.address

        with(holder.itemView) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    fun addDevice(device: BluetoothDevice){
        BluetoothDevices.addItem(device)
        values.add(device)
        notifyDataSetChanged()
    }

    override fun getItemCount() = values.size

    inner class BluetoothDeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.findViewById(R.id.id_text)
        val contentView: TextView = view.findViewById(R.id.content)
    }
}