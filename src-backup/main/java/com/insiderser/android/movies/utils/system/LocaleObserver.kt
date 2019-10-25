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
import android.content.Intent.ACTION_LOCALE_CHANGED
import android.content.IntentFilter
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleObserver @Inject constructor(application: Application) {
    
    private val callbacks = mutableListOf<WeakReference<LocaleChangedCallback>>()
    
    init {
        application.registerReceiver(LocaleBroadcastReceiver(), IntentFilter(ACTION_LOCALE_CHANGED))
    }
    
    fun addCallback(callback: LocaleChangedCallback) {
        synchronized(callback) {
            callbacks += WeakReference(callback)
        }
    }
    
    private fun notifyLocaleChanged() {
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
    
    private inner class LocaleBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == ACTION_LOCALE_CHANGED) {
                notifyLocaleChanged()
            }
        }
    }
}

typealias LocaleChangedCallback = () -> Unit