/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.utils.system

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkObserver @Inject constructor(
        application: Application,
        networkInfoProvider: NetworkInfoProvider) {
    
    private val callbacks = mutableListOf<WeakReference<NetworkAvailableCallback>>()
    
    init {
        if(SDK_INT >= LOLLIPOP) {
            val connectivityManager = ContextCompat.getSystemService(application,
                    ConnectivityManager::class.java) !!
            
            val networkRequest = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
            
            connectivityManager.registerNetworkCallback(networkRequest,
                    NetworkAvailableConnectivityManagerCallback())
        } else {
            @Suppress("DEPRECATION")
            application.registerReceiver(NetworkBroadcastReceiver(networkInfoProvider),
                    IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
    }
    
    fun addCallback(callback: NetworkAvailableCallback) {
        synchronized(callbacks) {
            callbacks += WeakReference(callback)
        }
    }
    
    private fun notifyNetworkAvailable() {
        synchronized(callbacks) {
            val iterator = callbacks.iterator()
            
            while(iterator.hasNext()) {
                val reference = iterator.next()
                val value = reference.get()
                
                if(value != null) {
                    value()
                } else {
                    iterator.remove()
                }
            }
        }
    }
    
    @RequiresApi(LOLLIPOP)
    private inner class NetworkAvailableConnectivityManagerCallback :
            ConnectivityManager.NetworkCallback() {
        
        override fun onAvailable(network: Network?) {
            GlobalScope.launch(Dispatchers.Main) {
                notifyNetworkAvailable()
            }
        }
    }
    
    private inner class NetworkBroadcastReceiver(
            private val networkInfoProvider: NetworkInfoProvider) : BroadcastReceiver() {
        
        override fun onReceive(context: Context?, intent: Intent?) {
            if(networkInfoProvider.isNetworkAvailable) {
                notifyNetworkAvailable()
            }
        }
    }
}

typealias NetworkAvailableCallback = () -> Unit