/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.glide

import android.app.ActivityManager
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.KITKAT
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.MemoryCategory
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class MyAppGlideModule : AppGlideModule() {
    
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val decodeFormat =
                if(isDeviceHighPerforming(context)) {
                    DecodeFormat.PREFER_ARGB_8888
                } else {
                    DecodeFormat.PREFER_RGB_565
                }
        
        builder.setDefaultRequestOptions(RequestOptions().format(decodeFormat))
        
        val diskCacheSize = MAX_DISK_CACHE_SIZE
        
        builder.setDiskCache(ExternalPreferredCacheDiskCacheFactory(context, diskCacheSize))
    }
    
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val memoryCategory =
                if(isDeviceHighPerforming(context)) {
                    MemoryCategory.NORMAL
                } else {
                    MemoryCategory.LOW
                }
        
        glide.setMemoryCategory(memoryCategory)
    }
    
    private fun isDeviceHighPerforming(context: Context): Boolean {
        val activityManager = ContextCompat.getSystemService(context,
                ActivityManager::class.java) !!
        
        // true if at least Android version is KitKat,
        //                  Android doesn't think device is low memory
        //                  4 cores,
        //                  memory per application is 128mb
        return SDK_INT >= KITKAT
                && ! activityManager.isLowRamDevice
                && Runtime.getRuntime().availableProcessors() >= 4
                && activityManager.memoryClass >= 128
    }
    
    override fun isManifestParsingEnabled(): Boolean = false
}

private const val MAX_DISK_CACHE_SIZE = 500L * 1024 * 1024