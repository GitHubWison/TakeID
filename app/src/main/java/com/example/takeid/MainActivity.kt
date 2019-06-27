package com.example.takeid

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.takeidlibrary.TakeIDRes
import com.example.takeidlibrary.TakeIDV2Activity
import com.example.takeidlibrary.TakeIdConst

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registBroadcast()
    }

    private fun registBroadcast() {
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val result = intent?.getSerializableExtra(TakeIdConst.TAKEID_EXTRA_NAME) as TakeIDRes
                Log.e("****","${result.picExtraName}==${result.txt}")
            }

        }, IntentFilter().apply { addAction(TakeIdConst.ACTION) })
    }

    fun toCamera(view: View) {
        TakeIDV2Activity.start(this)
    }
}
