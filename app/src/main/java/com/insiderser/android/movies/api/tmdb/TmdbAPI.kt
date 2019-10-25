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

package com.insiderser.android.movies.api.tmdb

import com.insiderser.android.movies.BuildConfig
import com.insiderser.android.movies.model.response.tmdb.TmdbSearch
import com.insiderser.android.movies.model.response.tmdb.genre.TmdbGenres
import com.insiderser.android.movies.model.response.tmdb.movie.TmdbMovieDetails
import com.insiderser.android.movies.model.response.tmdb.movie.TmdbMovies
import com.insiderser.android.movies.model.response.tmdb.tv.TmdbTvShowDetails
import com.insiderser.android.movies.model.response.tmdb.tv.TmdbTvShows
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface TmdbAPI {
    
    @GET("/3/discover/movie?api_key=$API_KEY")
    fun getMovies(@QueryMap params: Map<String, String> = emptyMap()): Call<TmdbMovies>
    
    @GET("/3/movie/{id}?api_key=$API_KEY&append_to_response=videos,reviews,images,recommendations,similar,release_dates,credits&include_image_language=null")
    fun getMovieDetails(
            @Path("id") movieId: Int,
            @Query(PARAM_LANGUAGE) language: String): Call<TmdbMovieDetails>
    
    @GET("/3/discover/tv?api_key=$API_KEY")
    fun getTvShows(@QueryMap params: Map<String, String> = emptyMap()): Call<TmdbTvShows>
    
    @GET("/3/tv/{id}?api_key=$API_KEY&append_to_response=videos,reviews,images,recommendations,similar,credits,external_ids&include_image_language=null")
    fun getTvShowDetails(
            @Path("id") movieId: Int,
            @Query(PARAM_LANGUAGE) language: String): Call<TmdbTvShowDetails>
    
    // TODO move to global search, not just movie.
    //  See https://developers.themoviedb.org/3/search/multi-search
    //  It probably involves creating custom GSON deserializer.
    @GET("/3/search/movie?api_key=$API_KEY")
    fun search(
            @Query("query") query: String,
            @QueryMap otherParams: Map<String, String> = emptyMap()): Call<TmdbSearch>
    
    @GET("/3/genre/movie/list?api_key=$API_KEY")
    fun getMovieGenres(@Query("language") language: String): Call<TmdbGenres>
    
    @GET("/3/genre/tv/list?api_key=$API_KEY")
    fun getTvGenres(@Query("language") language: String): Call<TmdbGenres>
    
    companion object {
        
        private const val API_KEY: String = BuildConfig.TMDB_API_KEY
        
        const val PARAM_LANGUAGE = "language"
        const val PARAM_REGION = "region"
        const val PARAM_PAGE = "page"
        const val PARAM_SORT_BY = "sort_by"
        
        const val PAGE_FIRST = 1
    }
}