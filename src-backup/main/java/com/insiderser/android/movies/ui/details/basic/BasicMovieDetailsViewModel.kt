/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.ui.details.basic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.insiderser.android.movies.data.repository.FavouritesRepository
import com.insiderser.android.movies.data.repository.MovieDetailsRepository
import com.insiderser.android.movies.model.MovieDetails
import com.insiderser.android.movies.model.NO_MOVIE_ID
import com.insiderser.android.movies.model.VideoType
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

class BasicMovieDetailsViewModel @Inject constructor(
        private val movieDetailsRepository: MovieDetailsRepository,
        private val favouritesRepository: FavouritesRepository,
        private val networkObserver: NetworkObserver,
        localeObserver: LocaleObserver,
        private val networkInfoProvider: NetworkInfoProvider) : CoroutineScopeViewModel(),
        StarView.Source {
    
    private val _movieDetails = MediatorLiveData<MovieDetails>()
    val movieDetails: LiveData<MovieDetails>
        get() = _movieDetails
    
    private val _isMovieInFavourites = MediatorLiveData<Boolean>()
    override val isStarChecked: LiveData<Boolean>
        get() = _isMovieInFavourites
    
    private val _isReloading = MutableLiveData<Boolean>()
    val isReloading: LiveData<Boolean> get() = _isReloading
    
    var movieId: Int = NO_MOVIE_ID
        private set
    
    private var currentMovieDetails by LiveDataManager(_movieDetails)
    
    private var currentIsInFavourites by LiveDataManager(_isMovieInFavourites)
    
    private val localeChangedCallback = { reloadMovieDetails() }
    
    private var networkAvailableCallback: NetworkAvailableCallback? = null
    
    init {
        localeObserver.addCallback(localeChangedCallback)
    }
    
    fun initState(movieId: Int) {
        if(this.movieId != movieId) {
            this.movieId = movieId
            
            initInternal()
        }
    }
    
    private fun initInternal() {
        checkState()
        
        currentMovieDetails = movieDetailsRepository.getMovieDetails(movieId, coroutineJob)
                .map { movieDetails ->
                    movieDetails?.copy(
                            videos = movieDetails.videos.filter { it.type == VideoType.TRAILER })
                }
        currentIsInFavourites = favouritesRepository.isMovieInFavourites(movieId)
        
        reloadMovieDetails()
    }
    
    fun reloadMovieDetails() {
        if(movieId == NO_MOVIE_ID) return
        
        if(networkInfoProvider.isNetworkAvailable) {
            launch {
                _isReloading.postValue(true)
                
                movieDetailsRepository.reloadMovieDetails(movieId)
                
                _isReloading.postValue(false)
            }
        } else {
            registerNetworkCallback()
        }
    }
    
    @Synchronized
    private fun registerNetworkCallback() {
        if(networkAvailableCallback == null) {
            networkAvailableCallback = {
                reloadMovieDetails()
                
                networkAvailableCallback = null
            }
            
            networkObserver.addCallback(networkAvailableCallback !!)
        }
    }
    
    private fun checkState() {
        check(movieId != NO_MOVIE_ID) { "Movie ID must be initialized" }
    }
    
    override fun onChecked() {
        checkState()
        
        if(_isMovieInFavourites.value != false) return
        
        favouritesRepository.addMovieToFavourites(movieId)
    }
    
    override fun onUnchecked() {
        checkState()
        
        if(_isMovieInFavourites.value != true) return
        
        favouritesRepository.deleteMovieFromFavourites(movieId)
    }
}