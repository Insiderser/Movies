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

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.insiderser.android.movies.data.preferences.PreferencesHelper
import com.insiderser.android.movies.data.repository.DiscoverRepository
import com.insiderser.android.movies.model.NO_LIMIT
import com.insiderser.android.movies.model.Poster
import com.insiderser.android.movies.model.types.DiscoverType
import com.insiderser.android.movies.model.types.Type
import com.insiderser.android.movies.ui.PosterListFragment
import com.insiderser.android.movies.utils.CoroutineScopeViewModel
import com.insiderser.android.movies.utils.LiveDataManager
import com.insiderser.android.movies.utils.system.NetworkInfoProvider
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
        private val discoverRepository: DiscoverRepository,
        private val preferencesHelper: PreferencesHelper,
        private val networkInfoProvider: NetworkInfoProvider) : CoroutineScopeViewModel(),
        PosterListFragment.Source {
    
    private val _posters = MediatorLiveData<List<Poster>>()
    override val posters: LiveData<List<Poster>> get() = _posters
    private var currentPosters: LiveData<List<Poster>>? by LiveDataManager(_posters) { value ->
        if(limit != NO_LIMIT) value.take(limit) else value
    }
    
    private val _isRefreshing = MutableLiveData<Boolean>()
    override val isRefreshing: LiveData<Boolean> get() = _isRefreshing
    
    private val preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if(! isInitialized) return@OnSharedPreferenceChangeListener
                
                val isPreferenceKeySortBy = when(type) {
                    Type.MOVIE -> PreferencesHelper.isPreferenceKeyMovieSortBy(key)
                    Type.TV_SHOW -> PreferencesHelper.isPreferenceKeyTvShowSortBy(key)
                }
                
                if(isPreferenceKeySortBy) {
                    reload()
                }
            }
    
    private lateinit var type: Type
    
    private var limit: Int = NO_LIMIT
    
    init {
        posters.observeForever { loadedPosters: List<Poster>? ->
            if(loadedPosters != null && loadedPosters.isNotEmpty()
                    && networkInfoProvider.isNetworkAvailable) {
                _isRefreshing.value = false
            }
        }
    }
    
    fun initState(type: Type, limit: Int = NO_LIMIT) {
        if(! isInitialized) {
            this.type = type
            this.limit = limit
            
            init()
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun init() {
        checkInitialized()
        
        reload()
        
        preferencesHelper.registerOnPreferenceChangedListener(preferenceChangeListener)
    }
    
    override fun reload() {
        checkInitialized()
        
        currentPosters = discoverRepository.getPosters(discoverType)
        
        val isNetworkAvailable = networkInfoProvider.isNetworkAvailable
        
        _isRefreshing.value = isNetworkAvailable
        
        if(isNetworkAvailable) {
            launch {
                discoverRepository.reload(discoverType)
            }
        }
    }
    
    private val discoverType get() = DiscoverType(type, preferencesHelper)
    
    private val isInitialized get() = this::type.isInitialized
    
    private fun checkInitialized() {
        check(isInitialized) {
            "You have to call DiscoverViewModel.initState before proceeding"
        }
    }
}