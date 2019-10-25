/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.insiderser.android.movies.data.repository.QueriesRepository
import com.insiderser.android.movies.data.repository.SearchRepository
import com.insiderser.android.movies.model.Movie
import com.insiderser.android.movies.ui.MoviesListFragment
import com.insiderser.android.movies.utils.CoroutineScopeViewModel
import com.insiderser.android.movies.utils.system.NetworkInfoProvider
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor(
        private val searchRepository: SearchRepository,
        queriesRepository: QueriesRepository,
        private val networkInfoProvider: NetworkInfoProvider) : CoroutineScopeViewModel(),
        MoviesListFragment.Source {
    
    private var query: String = ""
        set(value) {
            if(value != field) {
                field = value
                reloadMovies()
            }
        }
    
    private val _movies = MutableLiveData<List<Movie>>()
    override val movies: LiveData<List<Movie>> get() = _movies
    
    private val _isRefreshing = MutableLiveData<Boolean>()
    override val isRefreshing: LiveData<Boolean> get() = _isRefreshing
    
    init {
        queriesRepository.lastQuery.observeForever { lastQuery ->
            query = lastQuery ?: ""
        }
        
        movies.observeForever { loadedMovies ->
            if(loadedMovies.isNotEmpty() && networkInfoProvider.isNetworkAvailable) {
                _isRefreshing.value = false
            }
        }
    }
    
    override fun reloadMovies() {
        launch {
            _isRefreshing.postValue(true)
            
            val foundMovies =
                    if(query.isNotEmpty()) {
                        searchRepository.searchMovies(query)
                                .filter { it.posterPath != null }
                    } else emptyList()
            
            if(isActive) {
                _movies.postValue(foundMovies)
            }
            
            _isRefreshing.postValue(false)
        }
    }
}