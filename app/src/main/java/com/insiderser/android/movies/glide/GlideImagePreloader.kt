/*
 * Copyright (c) 2019 Oleksandr Bezushko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall
 * be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.glide

import android.app.ActivityManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import java.util.WeakHashMap
import kotlin.math.max
import kotlin.math.min

class GlideImagePreloader<T : Any> private constructor(private val source: Source<T>,
        private val imageWidth: Int, private val imageHeight: Int,
        private val activityManager: ActivityManager) :
        RecyclerView.OnScrollListener() {
    
    private var lastVisibleImagePosition: Int = - 1
    private var lastLoadedImagePositionFull: Int = - 1
    
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val layoutManager = recyclerView.layoutManager
        
        check(layoutManager is LinearLayoutManager) {
            "Layout managers other than LinearLayoutManager aren't supported; " +
                    "your was ${layoutManager?.javaClass?.name}"
        }
        
        val currentLastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        
        val maxPreload = calculateMaxPreload(recyclerView)
        
        if(currentLastVisibleItemPosition > lastVisibleImagePosition) {
            val fullPreloadFrom = max(currentLastVisibleItemPosition + 1,
                    lastLoadedImagePositionFull)
            val fullPreloadTo = min(currentLastVisibleItemPosition + maxPreload,
                    source.imageCount - 1)
            preload(fullPreloadFrom, fullPreloadTo)
            
            lastLoadedImagePositionFull = max(lastLoadedImagePositionFull, fullPreloadTo)
        }
        
        lastVisibleImagePosition = currentLastVisibleItemPosition
    }
    
    private fun preload(from: Int, to: Int) {
        for(position in from..to) {
            preload(position)
        }
    }
    
    private fun preload(position: Int) {
        val item = source.getItemForPosition(position) ?: return
        val glideRequest = source.getGlideRequest(item)
        
        glideRequest.preload(imageWidth, imageHeight)
    }
    
    private fun calculateMaxPreload(recyclerView: RecyclerView): Int {
        val layoutManager = recyclerView.layoutManager
        val spanCount = if(layoutManager is GridLayoutManager) layoutManager.spanCount else 1
        
        val itemsPerSpan = if(isDeviceLowOnMemory) ITEMS_PER_SPAN_LOW_MEMORY else ITEMS_PER_SPAN
        
        return spanCount * itemsPerSpan
    }
    
    private val isDeviceLowOnMemory: Boolean
        get() = ActivityManager.MemoryInfo().also { memoryInfo ->
            activityManager.getMemoryInfo(memoryInfo)
        }.lowMemory
    
    companion object {
        
        private const val ITEMS_PER_SPAN = 3
        private const val ITEMS_PER_SPAN_LOW_MEMORY = 1
        
        private val preloaders: MutableMap<RecyclerView, WeakReference<GlideImagePreloader<*>>> = WeakHashMap()
        
        @JvmStatic
        @Suppress("ReplacePutWithAssignment")
        fun <T : Any> attach(source: Source<T>, recyclerView: RecyclerView, imageWidth: Int,
                imageHeight: Int) {
            val activityManager = ContextCompat.getSystemService(recyclerView.context,
                    ActivityManager::class.java) !!
            
            val preloader = GlideImagePreloader(source, imageWidth, imageHeight, activityManager)
            recyclerView.addOnScrollListener(preloader)
            
            preloaders.put(recyclerView, WeakReference(preloader))
        }
        
        @JvmStatic
        @Suppress("ReplaceGetOrSet")
        fun detach(recyclerView: RecyclerView) {
            preloaders.get(recyclerView)?.get()?.let { preloader ->
                recyclerView.removeOnScrollListener(preloader)
                preloaders.remove(recyclerView)
            }
        }
    }
    
    interface Source<T : Any> {
        
        val imageCount: Int
        
        fun getItemForPosition(position: Int): T?
        
        fun getGlideRequest(item: T): GlideRequest<Drawable>
    }
}