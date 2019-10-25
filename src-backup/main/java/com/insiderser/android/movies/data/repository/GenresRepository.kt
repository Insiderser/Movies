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
import com.insiderser.android.movies.utils.system.LocaleInfoProvider
import com.insiderser.android.movies.utils.extentions.toGenresEntity
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
            val genresCall = tmdbAPI.getGenres(localeInfoProvider.language)
            val response = genresCall.execute()
            
            if(response.isSuccessful) {
                val genres = response.body() !!.genres.map { it.toGenresEntity() }
                database.genresDao.setGenres(genres)
                return Result.success()
            }
            
            return if(response.code() in 500..599)
                Result.retry()
            else
                Result.failure()
        } catch(e: IOException) {
            // No internet available
            return Result.failure()
        }
    }
}