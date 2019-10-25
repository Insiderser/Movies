/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.ui.main

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.insiderser.android.movies.data.repository.QueriesRepository
import com.insiderser.android.movies.data.repository.SearchRepository
import com.insiderser.android.movies.utils.CoroutineScopeViewModel
import com.insiderser.android.movies.utils.extentions.toMovieSearchSuggestion
import com.insiderser.android.movies.utils.extentions.toPastQuerySearchSuggestion
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
        private val searchRepository: SearchRepository,
        private val queriesRepository: QueriesRepository) : CoroutineScopeViewModel() {
    
    private val _searchSuggestions = MediatorLiveData<List<SearchSuggestion>>()
    val searchSuggestions: LiveData<List<SearchSuggestion>> get() = _searchSuggestions
    
    private val _isRefreshingSuggestions = MutableLiveData<Boolean>()
    val isRefreshingSuggestions: LiveData<Boolean> get() = _isRefreshingSuggestions
    
    var lastSubmittedQuery: String = ""
        private set
    
    var query: String = "to be reassigned"
        set(value) {
            val previousValue = field
            
            field = value
            
            if(value.isEmpty() && previousValue.isEmpty()) return
            
            if(value.isEmpty()) {
                onQueryEmpty()
            } else {
                if(previousValue.isEmpty()) {
                    onQueryNotEmpty()
                }
                
                if(_isRefreshingSuggestions.value != true) {
                    reloadSuggestions()
                }
            }
        }
    
    private val pastQueries = queriesRepository.pastQueries
    
    init {
        query = ""
    }
    
    fun onQuerySubmit() {
        lastSubmittedQuery = query.trim().also { query ->
            if(query.isNotEmpty()) {
                queriesRepository.addQuery(query)
            }
        }
    }
    
    fun onPosterSuggestionClicked() {
        lastSubmittedQuery = ""
    }
    
    fun removeQueryFromSuggestions(query: String) {
        queriesRepository.removeQuery(query)
    }
    
    private fun onQueryEmpty() {
        cancelChildren()
        
        _searchSuggestions.addSource(pastQueries) { pastQueries ->
            _searchSuggestions.value = pastQueries.take(SUGGESTIONS_LIMIT_COUNT)
                    .map { it.toPastQuerySearchSuggestion() }
        }
    }
    
    private fun onQueryNotEmpty() {
        _searchSuggestions.removeSource(pastQueries)
    }
    
    @WorkerThread
    private fun reloadSuggestions() {
        launch {
            val queryWhenCalled = query
            
            if(queryWhenCalled.isEmpty())
                return@launch
            
            if(! isActive) return@launch
            
            _isRefreshingSuggestions.postValue(true)
            
            val foundMovieSuggestions = queriesRepository.searchQueries(queryWhenCalled,
                    PAST_QUERIES_LIMIT_COUNT)?.let { foundMovies ->
                foundMovies.mapTo(mutableListOf<SearchSuggestion>()) {
                    it.toPastQuerySearchSuggestion()
                }
            } ?: mutableListOf()
            
            if(! isActive) return@launch
            
            foundMovieSuggestions += searchRepository.searchMovies(queryWhenCalled,
                    limit = SUGGESTIONS_LIMIT_COUNT - foundMovieSuggestions.size)
                    .map { it.toMovieSearchSuggestion() }
            
            if(! isActive) return@launch
            
            _searchSuggestions.postValue(foundMovieSuggestions)
            
            if(query == queryWhenCalled) {
                _isRefreshingSuggestions.postValue(false)
                return@launch
            }
            
            reloadSuggestions()
        }
    }
    
    override fun cancelChildren() {
        super.cancelChildren()
        
        _isRefreshingSuggestions.postValue(false)
    }
    
    companion object {
        const val SUGGESTIONS_LIMIT_COUNT = 5
        const val PAST_QUERIES_LIMIT_COUNT = 2
    }
}