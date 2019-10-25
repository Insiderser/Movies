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

package com.insiderser.android.movies.ui.details.basic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.insiderser.android.movies.data.repository.FavouritesRepository
import com.insiderser.android.movies.data.repository.MovieDetailsRepository
import com.insiderser.android.movies.data.repository.TvShowsDetailsRepository
import com.insiderser.android.movies.model.NO_ID
import com.insiderser.android.movies.model.PosterDetails
import com.insiderser.android.movies.model.types.Type
import com.insiderser.android.movies.model.types.VideoType
import com.insiderser.android.movies.utils.CoroutineScopeViewModel
import com.insiderser.android.movies.utils.LiveDataManager
import com.insiderser.android.movies.utils.extentions.map
import com.insiderser.android.movies.utils.system.LocaleObserver
import com.insiderser.android.movies.utils.system.NetworkAvailableCallback
import com.insiderser.android.movies.utils.system.NetworkInfoProvider
import com.insiderser.android.movies.utils.system.NetworkObserver
import com.insiderser.android.movies.widget.StarView
import kotlinx.coroutines.launch
import javax.inject.Inject

class BasicDetailsViewModel @Inject constructor(
        private val movieDetailsRepository: MovieDetailsRepository,
        private val tvShowsDetailsRepository: TvShowsDetailsRepository,
        private val favouritesRepository: FavouritesRepository,
        private val networkObserver: NetworkObserver,
        localeObserver: LocaleObserver,
        private val networkInfoProvider: NetworkInfoProvider) : CoroutineScopeViewModel(),
        StarView.Source {
    
    private val _details = MediatorLiveData<PosterDetails>()
    val details: LiveData<PosterDetails>
        get() = _details
    
    private val _isInFavourites = MediatorLiveData<Boolean>()
    override val isStarChecked: LiveData<Boolean>
        get() = _isInFavourites
    
    private val _isReloading = MutableLiveData<Boolean>()
    val isReloading: LiveData<Boolean> get() = _isReloading
    
    var id: Int = NO_ID
        private set
    
    lateinit var type: Type
        private set
    
    private var currentDetails by LiveDataManager(_details)
    
    private var currentIsInFavourites by LiveDataManager(_isInFavourites)
    
    private val localeChangedCallback = { reloadMovieDetails() }
    
    private val networkAvailableCallback: NetworkAvailableCallback = { reloadMovieDetails() }
    
    init {
        localeObserver.addCallback(localeChangedCallback)
    }
    
    fun initState(id: Int, type: Type) {
        if(this.id != id) {
            this.id = id
            this.type = type
            
            initInternal()
        }
    }
    
    private fun initInternal() {
        checkState()
        
        currentDetails =
                when(type) {
                    Type.MOVIE -> movieDetailsRepository.getMovieDetails(id, coroutineJob)
                            .map { movieDetails ->
                                movieDetails?.copy(
                                        videos = movieDetails.videos.filter { it.type == VideoType.TRAILER })
                            }
                    Type.TV_SHOW -> tvShowsDetailsRepository.getTvShowDetails(id, coroutineJob)
                            .map { tvShowDetails ->
                                tvShowDetails?.copy(
                                        videos = tvShowDetails.videos.filter { it.type == VideoType.TRAILER })
                            }
                }
        
        currentIsInFavourites = favouritesRepository.isInFavourites(id)
        
        reloadMovieDetails()
    }
    
    private fun reloadMovieDetails() {
        if(id == NO_ID) return
        
        launch {
            _isReloading.postValue(true)
            
            when(type) {
                Type.MOVIE -> movieDetailsRepository.reloadMovieDetails(id)
                Type.TV_SHOW -> tvShowsDetailsRepository.reloadTvShowDetails(id)
            }
            
            _isReloading.postValue(false)
        }
        
        if(! networkInfoProvider.isNetworkAvailable) {
            registerNetworkCallback()
        }
    }
    
    private fun registerNetworkCallback() {
        networkObserver.addCallback(networkAvailableCallback)
    }
    
    private fun checkState() {
        check(id != NO_ID) { "ID must be initialized" }
    }
    
    override fun onChecked() {
        checkState()
        
        if(_isInFavourites.value != false) return
        
        favouritesRepository.addToFavourites(id, Type.MOVIE)
    }
    
    override fun onUnchecked() {
        checkState()
        
        if(_isInFavourites.value != true) return
        
        favouritesRepository.deleteFromFavourites(id)
    }
}