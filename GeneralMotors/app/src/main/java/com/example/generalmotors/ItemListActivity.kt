package com.example.generalmotors

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.widget.NestedScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.generalmotors.data.BluetoothDevice
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
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        recyclerView = findViewById(R.id.item_list)
        setupRecyclerView()
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

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
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

    private fun setupRecyclerView() {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, BluetoothDevices.ITEMS, twoPane)
    }

    class SimpleItemRecyclerViewAdapter(private val parentActivity: ItemListActivity, private val values: List<BluetoothDevice>, private val twoPane: Boolean) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
            val item = v.tag as BluetoothDevice
            if (twoPane) {
                val fragment = ItemDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(ItemDetailFragment.ARG_ITEM_ID, item.deviceName)
                    }
                }
                parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit()
            } else {
                val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                    putExtra(ItemDetailFragment.ARG_ITEM_ID, item.deviceName)
                }
                v.context.startActivity(intent)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = item.deviceName
            holder.contentView.text = item.content

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.findViewById(R.id.id_text)
            val contentView: TextView = view.findViewById(R.id.content)
        }
    }
}