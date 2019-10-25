/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.ui.reviews

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.insiderser.android.movies.data.repository.MovieDetailsRepository
import com.insiderser.android.movies.model.Review
import com.insiderser.android.movies.model.NO_MOVIE_ID
import com.insiderser.android.movies.utils.CoroutineScopeViewModel
import com.insiderser.android.movies.utils.extentions.map
import com.insiderser.android.movies.utils.system.NetworkAvailableCallback
import com.insiderser.android.movies.utils.system.NetworkInfoProvider
import com.insiderser.android.movies.utils.system.NetworkObserver
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReviewsViewModel @Inject constructor(
        private val movieDetailsRepository: MovieDetailsRepository,
        private val networkObserver: NetworkObserver,
        private val networkInfoProvider: NetworkInfoProvider) : CoroutineScopeViewModel() {
    
    private var movieId: Int = NO_MOVIE_ID
    
    private lateinit var _reviews: LiveData<List<Review>>
    val reviews: LiveData<List<Review>>
        get() {
            checkState()
            return _reviews
        }
    
    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> get() = _isRefreshing
    
    private var networkAvailableCallback: NetworkAvailableCallback? = null
    
    fun initState(movieId: Int) {
        if(this.movieId == NO_MOVIE_ID) {
            this.movieId = movieId
            
            init()
        }
    }
    
    private fun init() {
        checkState()
        
        val movieDetails = movieDetailsRepository.getMovieDetails(movieId, coroutineJob)
        
        _reviews = movieDetails.map { it?.reviews }
    }
    
    fun reloadReviews() {
        checkState()
        
        if(networkInfoProvider.isNetworkAvailable) {
            launch {
                _isRefreshing.postValue(true)
                
                movieDetailsRepository.reloadMovieDetails(movieId)
                
                _isRefreshing.postValue(false)
            }
        } else {
            registerNetworkCallback()
        }
    }
    
    @Synchronized
    private fun registerNetworkCallback() {
        if(networkAvailableCallback == null) {
            networkAvailableCallback = {
                reloadReviews()
                
                networkAvailableCallback = null
            }
            
            networkObserver.addCallback(networkAvailableCallback !!)
        }
    }
    
    private fun checkState() {
        check(movieId != NO_MOVIE_ID) { "Movie ID must be initialized" }
    }
}