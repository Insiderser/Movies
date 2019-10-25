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
import androidx.lifecycle.MediatorLiveData
import com.insiderser.android.movies.api.tmdb.TmdbAPI
import com.insiderser.android.movies.data.database.AppDatabase
import com.insiderser.android.movies.model.entity.TvShowsEntity
import com.insiderser.android.movies.model.tv.TvShow
import com.insiderser.android.movies.model.tv.TvShowDetails
import com.insiderser.android.movies.utils.extentions.toGenresEntity
import com.insiderser.android.movies.utils.extentions.toTvShowEntity
import com.insiderser.android.movies.utils.system.LocaleInfoProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvShowsDetailsRepository @Inject constructor(
        private val database: AppDatabase,
        private val tmdbAPI: TmdbAPI,
        private val localeInfoProvider: LocaleInfoProvider,
        private val genresDataSource: GenresDataSource) {
    
    private val showIdsReloading = mutableSetOf<Int>()
    
    fun getTvShowDetails(tvShowId: Int, parentJob: Job? = null): LiveData<TvShowDetails> =
            MediatorLiveData<TvShowDetails>().also { convertedLiveData ->
                val show = database.tvShowsDao.getShow(tvShowId)
                
                val currentJob = Job(parentJob)
                
                val coroutineScope = CoroutineScope(currentJob + Dispatchers.IO)
                
                convertedLiveData.addSource(show) { loadedShow ->
                    currentJob.cancelChildren()
                    
                    coroutineScope.launch {
                        if(loadedShow == null) {
                            convertedLiveData.postValue(null)
                            return@launch
                        }
                        
                        val convertedShow = convertedLiveData.value
                        
                        val showGenres = genresDataSource.genres.value?.let { genres ->
                            loadedShow.genreIds.mapNotNull { genres[it] }
                        } ?: emptyList()
                        
                        val oldRecommendations = convertedShow?.recommendations
                                ?: emptyList()
                        val oldSimilarShows = convertedShow?.similar ?: emptyList()
                        var convertedShowDetails =
                                loadedShow.toTvShowDetails(showGenres, oldRecommendations,
                                        oldSimilarShows)
                        
                        if(! currentJob.isActive) return@launch
                        
                        convertedLiveData.postValue(convertedShowDetails)
                        
                        val recommendations = database.tvShowsDao.getShows(
                                loadedShow.recommendationIds)
                        
                        if(! currentJob.isActive) return@launch
                        
                        if(recommendations != oldRecommendations) {
                            convertedShowDetails = convertedShowDetails.copy(
                                    recommendations = recommendations)
                            convertedLiveData.postValue(convertedShowDetails)
                        }
                        
                        val similarShows = database.tvShowsDao.getShows(
                                loadedShow.similarTvShowIds)
                        
                        if(! currentJob.isActive) return@launch
                        
                        if(similarShows != oldSimilarShows) {
                            convertedShowDetails = convertedShowDetails.copy(similar = similarShows)
                            convertedLiveData.postValue(convertedShowDetails)
                        }
                    }
                }
                
                convertedLiveData.addSource(genresDataSource.genres) { genres ->
                    coroutineScope.launch {
                        val currentConvertedShow = convertedLiveData.value ?: return@launch
                        val currentShow = show.value ?: return@launch
                        val tvShowGenres = currentShow.genreIds.mapNotNull { genres[it] }
                        
                        if(currentJob.isActive && convertedLiveData.value?.genres != tvShowGenres) {
                            convertedLiveData.postValue(
                                    currentConvertedShow.copy(genres = tvShowGenres))
                        }
                    }
                }
            }
    
    private fun TvShowsEntity.toTvShowDetails(genres: List<String>, recommendations: List<TvShow>,
            similarMovies: List<TvShow>) = TvShowDetails(id, title, overview, rating, "",
            posterPath, backdropPaths.getOrNull(0), genres, backdropPaths, reviews, videos,
            recommendations, similarMovies, seasons, cast, crew, isFullyCached)
    
    @WorkerThread
    fun reloadTvShowDetails(showId: Int) {
        synchronized(showIdsReloading) {
            if(showIdsReloading.contains(showId))
                return
            
            showIdsReloading.add(showId)
        }
        
        try {
            val language = localeInfoProvider.language
            
            val call = tmdbAPI.getTvShowDetails(showId, language)
            val response = call.execute()
            
            if(response.isSuccessful) {
                database.runInTransaction {
                    val body = response.body() !!
                    
                    database.tvShowsDao.also { dao ->
                        dao.update(body.toTvShowEntity())
                        dao.insertOrUpdate(body.recommendations.results)
                        dao.insertOrUpdate(body.similarTvShows.results)
                    }
                    
                    body.genres.map { it.toGenresEntity() }.let { genres ->
                        database.genresDao.insertOrUpdate(genres)
                    }
                }
            }
        } catch(e: IOException) {
            // The Internet probably down.
        } finally {
            synchronized(showIdsReloading) {
                showIdsReloading.remove(showId)
            }
        }
    }
}