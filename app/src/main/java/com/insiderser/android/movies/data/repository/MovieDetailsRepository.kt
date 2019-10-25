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
import com.insiderser.android.movies.model.entity.MoviesEntity
import com.insiderser.android.movies.model.movie.Movie
import com.insiderser.android.movies.model.movie.MovieDetails
import com.insiderser.android.movies.utils.extentions.map
import com.insiderser.android.movies.utils.extentions.toGenresEntity
import com.insiderser.android.movies.utils.extentions.toMoviesEntity
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
class MovieDetailsRepository @Inject constructor(
        private val database: AppDatabase,
        private val tmdbAPI: TmdbAPI,
        private val localeInfoProvider: LocaleInfoProvider) {
    
    private val genres: LiveData<Map<Int, String>> = database.genresDao.getGenres().map { genres ->
        genres?.associate { it.id to it.name }
    }
    
    private val movieIdsReloading = mutableSetOf<Int>()
    
    fun getMovieDetails(movieId: Int, parentJob: Job? = null): LiveData<MovieDetails> =
            MediatorLiveData<MovieDetails>().also { convertedLiveData ->
                val movie = database.moviesDao.getMovie(movieId)
                
                val currentJob = Job(parentJob)
                
                val coroutineScope = CoroutineScope(currentJob + Dispatchers.IO)
                
                convertedLiveData.addSource(movie) { loadedMovie ->
                    currentJob.cancelChildren()
                    
                    coroutineScope.launch {
                        if(loadedMovie == null) {
                            convertedLiveData.postValue(null)
                            return@launch
                        }
                        
                        val convertedMovie = convertedLiveData.value
                        
                        val movieGenres = genres.value?.let { genres ->
                            loadedMovie.genreIds.mapNotNull { genres[it] }
                        } ?: emptyList()
                        
                        val oldRecommendations = convertedMovie?.recommendations
                                ?: emptyList()
                        val oldSimilarMovies = convertedMovie?.similar ?: emptyList()
                        var convertedMovieDetails =
                                loadedMovie.toMovieDetails(movieGenres, oldRecommendations,
                                        oldSimilarMovies)
                        
                        if(! currentJob.isActive) return@launch
                        
                        convertedLiveData.postValue(convertedMovieDetails)
                        
                        val recommendations = database.moviesDao.getMovies(
                                loadedMovie.recommendationIds)
                        
                        if(! currentJob.isActive) return@launch
                        
                        if(recommendations != oldRecommendations) {
                            convertedMovieDetails = convertedMovieDetails.copy(
                                    recommendations = recommendations)
                            convertedLiveData.postValue(convertedMovieDetails)
                        }
                        
                        val similarMovies = database.moviesDao.getMovies(
                                loadedMovie.similarMoviesIds)
                        
                        if(! currentJob.isActive) return@launch
                        
                        if(similarMovies != oldSimilarMovies) {
                            convertedMovieDetails = convertedMovieDetails.copy(
                                    similar = similarMovies)
                            convertedLiveData.postValue(convertedMovieDetails)
                        }
                    }
                }
                
                convertedLiveData.addSource(genres) { genres ->
                    coroutineScope.launch {
                        val currentConvertedMovie = convertedLiveData.value ?: return@launch
                        val currentMovie = movie.value ?: return@launch
                        val movieGenres = currentMovie.genreIds.mapNotNull { genres[it] }
                        
                        if(currentJob.isActive && convertedLiveData.value?.genres != movieGenres) {
                            convertedLiveData.postValue(
                                    currentConvertedMovie.copy(genres = movieGenres))
                        }
                    }
                }
            }
    
    private fun MoviesEntity.toMovieDetails(movieGenres: List<String>, recommendations: List<Movie>,
            similarMovies: List<Movie>) = MovieDetails(id, title, overview, rating,
            releaseYear.toString(), posterPath, backdropPaths, movieGenres, tagline, runtime,
            budget, revenue, imdbId, reviews, videos, recommendations, similarMovies, releaseDates,
            cast, crew, isMovieFullyCached)
    
    @WorkerThread
    fun reloadMovieDetails(movieId: Int) {
        synchronized(movieIdsReloading) {
            if(movieIdsReloading.contains(movieId))
                return
            
            movieIdsReloading.add(movieId)
        }
        
        try {
            val language = localeInfoProvider.language
            val region = localeInfoProvider.region
            
            val call = tmdbAPI.getMovieDetails(movieId, language)
            val response = call.execute()
            
            if(response.isSuccessful) {
                database.runInTransaction {
                    val body = response.body() !!
                    
                    database.moviesDao.also { moviesDao ->
                        moviesDao.update(body.toMoviesEntity(region, language))
                        moviesDao.insertOrUpdate(body.recommendations.results)
                        moviesDao.insertOrUpdate(body.similarMovies.results)
                    }
                    
                    body.genres.map { it.toGenresEntity() }.let { genres ->
                        database.genresDao.insertOrUpdate(genres)
                    }
                }
            }
        } catch(e: IOException) {
            // The Internet probably down.
        } finally {
            synchronized(movieIdsReloading) {
                movieIdsReloading.remove(movieId)
            }
        }
    }
}