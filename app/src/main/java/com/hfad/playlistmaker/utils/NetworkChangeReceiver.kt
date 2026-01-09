package com.hfad.playlistmaker.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.hfad.playlistmaker.R

class NetworkChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        intent?.action?.let { action ->
            if (action == CONNECTIVITY_ACTION) {
                val isConnected = NetworkUtils.isInternetAvailable(context)

                if (!isConnected) {
                    showToast(context)
                }
            }
        }
    }

    private fun showToast(context: Context) {
        Toast.makeText(
            context,
            R.string.no_internet,
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        const val CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
    }
}