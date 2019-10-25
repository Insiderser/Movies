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
import androidx.work.ListenableWorker.Result
import com.insiderser.android.movies.api.tmdb.TmdbAPI
import com.insiderser.android.movies.data.database.AppDatabase
import com.insiderser.android.movies.utils.extentions.toGenresEntity
import com.insiderser.android.movies.utils.system.LocaleInfoProvider
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenresRepository @Inject constructor(
        private val database: AppDatabase,
        private val tmdbAPI: TmdbAPI,
        private val localeInfoProvider: LocaleInfoProvider) {
    
    @WorkerThread
    fun reloadGenres(): Result {
        try {
            val language = localeInfoProvider.language
            
            val movieGenresCall = tmdbAPI.getMovieGenres(language)
            val tvGenresCall = tmdbAPI.getTvGenres(language)
            
            val movieGenresResponse = movieGenresCall.execute()
            
            val moviesResult = when {
                movieGenresResponse.isSuccessful -> {
                    val genres = movieGenresResponse.body() !!.genres.map { it.toGenresEntity() }
                    
                    database.genresDao.insertOrUpdate(genres)
                    
                    Result.success()
                }
                movieGenresResponse.code() in 500..599 -> Result.retry()
                else -> Result.failure()
            }
            
            val tvGenresResponse = tvGenresCall.execute()
            
            return when {
                tvGenresResponse.isSuccessful -> {
                    val genres = tvGenresResponse.body() !!.genres.map { it.toGenresEntity() }
                    
                    database.genresDao.insertOrUpdate(genres)
                    
                    moviesResult
                }
                tvGenresResponse.code() in 500..599 -> Result.retry()
                moviesResult is Result.Retry -> moviesResult // e.g. Retry
                else -> Result.failure()
            }
        } catch(e: IOException) {
            return Result.failure()
        }
    }
}