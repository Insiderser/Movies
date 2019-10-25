/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.data.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.insiderser.android.movies.api.tmdb.TmdbAPI
import com.insiderser.android.movies.data.database.AppDatabase
import com.insiderser.android.movies.data.preferences.PreferencesHelper
import com.insiderser.android.movies.model.Movie
import com.insiderser.android.movies.model.SortBy
import com.insiderser.android.movies.model.entity.DiscoverEntity
import com.insiderser.android.movies.utils.system.LocaleInfoProvider
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoverRepository @Inject constructor(
        private val database: AppDatabase,
        private val tmdbAPI: TmdbAPI,
        private val preferencesHelper: PreferencesHelper,
        private val localeInfoProvider: LocaleInfoProvider) {
    
    fun getMovies(): LiveData<List<Movie>> = database.discoverDao.getMovies()
    
    @WorkerThread
    fun loadMovies(page: Int = TmdbAPI.PAGE_FIRST) {
        try {
            val params = mapOf(
                    TmdbAPI.PARAM_LANGUAGE to localeInfoProvider.language,
                    TmdbAPI.PARAM_REGION to localeInfoProvider.region,
                    TmdbAPI.PARAM_SORT_BY to preferencesHelper.sortBy.toTmdbQueryString(),
                    TmdbAPI.PARAM_PAGE to page.toString())
            
            val moviesCall = tmdbAPI.getMovies(params)
            val response = moviesCall.execute()
            
            if(response.isSuccessful) {
                database.runInTransaction {
                    val loadedMovies = response.body() !!.movies
                    
                    database.moviesDao.insertOrUpdate(loadedMovies)
                    
                    val filteredMovies =
                            loadedMovies.filter { it.posterPath != null }
                    
                    val discoverMovies = List(filteredMovies.size) { index ->
                        val positionInList = index + (page - 1) * filteredMovies.size
                        DiscoverEntity(filteredMovies[index].id, positionInList)
                    }
                    
                    if(page == TmdbAPI.PAGE_FIRST) {
                        database.discoverDao.deleteAll()
                    }
                    database.discoverDao.insertAll(discoverMovies)
                }
            }
        } catch(e: IOException) {
            // The Internet probably gone
        }
    }
    
    private fun SortBy.toTmdbQueryString() = when(this) {
        SortBy.POPULARITY -> "popularity.desc"
        SortBy.REVENUE -> "revenue.desc"
        SortBy.VOTE_COUNT -> "vote_count.desc"
    }
}