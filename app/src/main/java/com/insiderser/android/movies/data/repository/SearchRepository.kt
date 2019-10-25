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

package com.insiderser.android.movies.data.repository

import androidx.annotation.WorkerThread
import com.insiderser.android.movies.api.tmdb.TmdbAPI
import com.insiderser.android.movies.data.database.AppDatabase
import com.insiderser.android.movies.model.NO_LIMIT
import com.insiderser.android.movies.model.movie.Movie
import com.insiderser.android.movies.utils.extentions.filter
import com.insiderser.android.movies.utils.extentions.toMovie
import com.insiderser.android.movies.utils.system.LocaleInfoProvider
import com.insiderser.android.movies.utils.system.NetworkInfoProvider
import java.io.IOException
import javax.inject.Inject

class SearchRepository @Inject constructor(
        private val database: AppDatabase,
        private val tmdbAPI: TmdbAPI,
        private val localeInfoProvider: LocaleInfoProvider,
        private val networkInfoProvider: NetworkInfoProvider) {
    
    @WorkerThread
    fun searchMovies(query: String, limit: Int = NO_LIMIT,
            filterMoviesWithNullPosterPath: Boolean = true): List<Movie> {
        return if(networkInfoProvider.isNetworkAvailable)
            remoteSearch(query, limit, filterMoviesWithNullPosterPath)
        else localSearch(query, limit, filterMoviesWithNullPosterPath)
    }
    
    @WorkerThread
    private fun remoteSearch(query: String, limit: Int,
            filterMoviesWithNullPosterPath: Boolean): List<Movie> {
        try {
            val requestParams = mapOf(
                    TmdbAPI.PARAM_LANGUAGE to localeInfoProvider.language,
                    TmdbAPI.PARAM_REGION to localeInfoProvider.region)
            
            val searchCall = tmdbAPI.search(query, requestParams)
            val response = searchCall.execute()
            
            if(response.isSuccessful) {
                val loadedMovies = response.body() !!.results
                
                database.moviesDao.insertOrUpdate(loadedMovies)
                
                return loadedMovies.filter(limit) {
                    (! filterMoviesWithNullPosterPath) || it.posterPath != null
                }.map { it.toMovie() }
            }
        } catch(e: IOException) {
            // The Internet probably gone
        }
        
        return emptyList()
    }
    
    @WorkerThread
    private fun localSearch(query: String, limit: Int,
            filterMoviesWithNullPosterPath: Boolean): List<Movie> {
        val words = query.split(Regex("""\s+"""))
        
        return if(words.isNotEmpty()) {
            database.moviesDao.searchMovies(words[0]).let { foundMovies ->
                val wordsToFilter = words.subList(1, words.size)
                
                foundMovies.filter(limit) { movie ->
                    if(filterMoviesWithNullPosterPath && movie.posterPath == null) return@filter false
                    
                    wordsToFilter.all {
                        movie.title.contains(it, ignoreCase = true)
                    }
                }.map { it.toMovie() }
            }
        } else emptyList()
    }
}