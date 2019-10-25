/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.ui.main

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.insiderser.android.movies.data.preferences.PreferencesHelper
import com.insiderser.android.movies.data.repository.DiscoverRepository
import com.insiderser.android.movies.ui.MoviesListFragment
import com.insiderser.android.movies.utils.CoroutineScopeViewModel
import com.insiderser.android.movies.utils.system.NetworkAvailableCallback
import com.insiderser.android.movies.utils.system.NetworkInfoProvider
import com.insiderser.android.movies.utils.system.NetworkObserver
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
        private val discoverRepository: DiscoverRepository,
        private val networkObserver: NetworkObserver,
        preferencesHelper: PreferencesHelper,
        private val networkInfoProvider: NetworkInfoProvider) : CoroutineScopeViewModel(),
        MoviesListFragment.Source {
    
    override val movies = discoverRepository.getMovies()
    
    private val _isRefreshing = MutableLiveData<Boolean>()
    override val isRefreshing: LiveData<Boolean> get() = _isRefreshing
    
    private val preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if(PreferencesHelper.isPreferenceKeySortBy(key)) {
                    reloadMovies()
                }
            }
    
    private var networkAvailableCallback: NetworkAvailableCallback? = null
    
    override fun reloadMovies() {
        val isNetworkAvailable = networkInfoProvider.isNetworkAvailable
        
        _isRefreshing.value = isNetworkAvailable
        
        if(isNetworkAvailable) {
            launch {
                discoverRepository.loadMovies()
            }
        } else {
            registerNetworkCallback()
        }
    }
    
    init {
        movies.observeForever { loadedMovies ->
            if(loadedMovies.isNotEmpty() && networkInfoProvider.isNetworkAvailable) {
                _isRefreshing.value = false
            }
        }
        
        reloadMovies()
        
        preferencesHelper.registerOnPreferenceChangedListener(preferenceChangeListener)
    }
    
    @Synchronized
    private fun registerNetworkCallback() {
        if(networkAvailableCallback == null) {
            networkAvailableCallback = {
                reloadMovies()
                
                networkAvailableCallback = null
            }
            
            networkObserver.addCallback(networkAvailableCallback !!)
        }
    }
}