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

package com.insiderser.android.movies.ui

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import com.insiderser.android.movies.data.repository.MovieDetailsRepository
import com.insiderser.android.movies.model.NO_ID
import com.insiderser.android.movies.model.movie.MovieDetails
import com.insiderser.android.movies.utils.CoroutineScopeViewModel

open class MovieDetailsViewModel(private val movieDetailsRepository: MovieDetailsRepository) :
        CoroutineScopeViewModel() {
    
    protected lateinit var loadedMovieDetails: LiveData<MovieDetails>
        private set
    
    var movieId: Int = NO_ID
        private set
    
    protected open fun initState(movieId: Int) {
        if(this.movieId == NO_ID) {
            this.movieId = movieId
            
            init()
        }
    }
    
    @CallSuper
    protected open fun init() {
        checkState()
        
        loadedMovieDetails = movieDetailsRepository.getMovieDetails(movieId, coroutineJob)
    }
    
    protected fun checkState() {
        check(movieId != NO_ID) { "Movie ID must be initialized" }
    }
}