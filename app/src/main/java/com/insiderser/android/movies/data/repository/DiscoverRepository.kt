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

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.work.ListenableWorker.Result
import com.insiderser.android.movies.api.tmdb.TmdbAPI
import com.insiderser.android.movies.data.database.AppDatabase
import com.insiderser.android.movies.model.Poster
import com.insiderser.android.movies.model.entity.DiscoverEntity
import com.insiderser.android.movies.model.response.tmdb.movie.TmdbMovie
import com.insiderser.android.movies.model.response.tmdb.tv.TmdbTvShow
import com.insiderser.android.movies.model.types.DiscoverType
import com.insiderser.android.movies.model.types.Type
import com.insiderser.android.movies.model.types.toType
import com.insiderser.android.movies.utils.system.LocaleInfoProvider
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoverRepository @Inject constructor(
        private val database: AppDatabase,
        private val tmdbAPI: TmdbAPI,
        private val localeInfoProvider: LocaleInfoProvider) {
    
    @Suppress("UNCHECKED_CAST")
    fun getPosters(type: DiscoverType) =
            database.discoverDao.run {
                when(type.toType()) {
                    Type.MOVIE -> getMovies(type)
                    Type.TV_SHOW -> getTvShows(type)
                }
            } as LiveData<List<Poster>>
    
    @WorkerThread
    fun reload(type: DiscoverType, page: Int = TmdbAPI.PAGE_FIRST): Result {
        try {
            val params = mapOf(
                    TmdbAPI.PARAM_LANGUAGE to localeInfoProvider.language,
                    TmdbAPI.PARAM_REGION to localeInfoProvider.region,
                    TmdbAPI.PARAM_SORT_BY to type.toTmdbQueryString(),
                    TmdbAPI.PARAM_PAGE to page.toString())
            
            val generalType = type.toType()
            
            val call = tmdbAPI.run {
                when(generalType) {
                    Type.MOVIE -> getMovies(params)
                    Type.TV_SHOW -> getTvShows(params)
                }
            }
            val response = call.execute()
            
            return when {
                response.isSuccessful -> {
                    database.runInTransaction {
                        val loadedResults = response.body() !!.results
                        
                        @Suppress("UNCHECKED_CAST")
                        when(generalType) {
                            Type.MOVIE -> database.moviesDao.insertOrUpdate(
                                    loadedResults as List<TmdbMovie>)
                            Type.TV_SHOW -> database.tvShowsDao.insertOrUpdate(
                                    loadedResults as List<TmdbTvShow>)
                        }
                        
                        val filteredMovies =
                                loadedResults.filter { it.posterPath != null }
                        
                        val discoverMovies = List(filteredMovies.size) { index ->
                            val positionInList = index + (page - 1) * filteredMovies.size
                            DiscoverEntity(filteredMovies[index].id, positionInList, type)
                        }
                        
                        if(page == TmdbAPI.PAGE_FIRST) {
                            database.discoverDao.deleteAll(type)
                        }
                        database.discoverDao.insertAll(discoverMovies)
                    }
                    
                    Result.success()
                }
                response.code() in 500..599 -> Result.retry()
                else -> Result.failure()
            }
        } catch(e: IOException) {
            // The Internet probably gone
            Log.w(TAG, e)
            return Result.failure()
        }
    }
    
    private fun DiscoverType.toTmdbQueryString() = when(this) {
        DiscoverType.MOVIE_POPULARITY, DiscoverType.TV_SHOW_POPULARITY -> "popularity.desc"
        DiscoverType.MOVIE_VOTE_COUNT -> "vote_count.desc"
        DiscoverType.MOVIE_REVENUE -> "revenue.desc"
        DiscoverType.TV_SHOW_VOTE_AVERAGE -> "vote_average.desc"
    }
    
    companion object {
        private val TAG: String = DiscoverRepository::class.java.name
    }
}