package com.example.generalmotors.data

import java.util.ArrayList
import java.util.HashMap

object BluetoothDevices {
    val ITEMS: MutableList<BluetoothDevice> = ArrayList()
    val ITEM_MAP: MutableMap<String, BluetoothDevice> = HashMap()


    fun addItem(item: BluetoothDevice) {
        if(!ITEM_MAP.containsKey(item.address)){
            ITEMS.add(item)
            ITEM_MAP[item.address] = item
        }
    }
}