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

package com.insiderser.android.movies.ui.main

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.ViewStubProxy
import com.insiderser.android.movies.R
import com.insiderser.android.movies.di.injector
import com.insiderser.android.movies.model.NO_LIMIT
import com.insiderser.android.movies.model.types.Type
import com.insiderser.android.movies.model.types.toInt
import com.insiderser.android.movies.model.types.toType
import com.insiderser.android.movies.ui.PosterListFragment
import com.insiderser.android.movies.utils.extentions.viewModelProvider

class DiscoverFragment : PosterListFragment() {
    
    override val source: DiscoverViewModel by viewModelProvider {
        injector.getDiscoverViewModel().also { viewModel ->
            val arguments = arguments
            
            check(arguments != null && arguments.containsKey(ARGUMENTS_TYPE)) {
                "Did you create DiscoverFragment using its constructor? " +
                        "Use DiscoverFragment.newInstance instead."
            }
            
            val type = arguments.getInt(ARGUMENTS_TYPE).toType()
            
            if(arguments.containsKey(ARGUMENTS_LIMIT)) {
                val limit = arguments.getInt(ARGUMENTS_LIMIT)
                viewModel.initState(type, limit)
            } else {
                viewModel.initState(type)
            }
        }
    }
    
    @LayoutRes override val emptyLayoutRes: Int = R.layout.empty_layout_discover
    
    override fun setEmptyViewVisibility(emptyView: ViewStubProxy, shouldBeVisible: Boolean) {
        if(shouldBeVisible) {
            val networkAvailable = injector.networkInfoProvider.isNetworkAvailable
            super.setEmptyViewVisibility(emptyView, shouldBeVisible = ! networkAvailable)
        } else
            super.setEmptyViewVisibility(emptyView, shouldBeVisible)
    }
    
    companion object {
        
        @JvmField
        val TAG_MOVIES: String = DiscoverFragment::class.java.name + "-movies"
        
        @JvmField
        val TAG_TV_SHOWS: String = DiscoverFragment::class.java.name + "-tv_shows"
        
        private const val ARGUMENTS_TYPE = "type"
        private const val ARGUMENTS_LIMIT = "limit"
        
        @JvmStatic
        fun newInstance(type: Type, limit: Int = NO_LIMIT) = DiscoverFragment().also { fragment ->
            fragment.arguments = Bundle().apply {
                putInt(ARGUMENTS_TYPE, type.toInt())
                
                if(limit != NO_LIMIT) {
                    putInt(ARGUMENTS_LIMIT, limit)
                }
            }
        }
    }
}