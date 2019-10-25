/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.api.tmdb

import android.app.Application
import android.content.Context
import android.net.Uri
import com.insiderser.android.movies.MoviesApp
import com.insiderser.android.movies.utils.ImageWidthProvider

object TmdbUriBuilder {
    
    private const val BASE_IMAGE_URL = "https://image.tmdb.org/t/p/"
    
    @JvmStatic
    fun buildPosterImageUri(applicationContext: Context, imageKey: String): Uri {
        val width = ImageWidthProvider.getPosterWidth(applicationContext)
        
        return buildPosterImageUri(width, imageKey)
    }
    
    @JvmStatic
    fun buildPosterImageUri(width: Int, imageKey: String): Uri {
        val sizeString = when(width) {
            in 1..92 -> "w92"
            in 93..154 -> "w154"
            in 155..185 -> "w185"
            in 186..342 -> "w342"
            in 343..500 -> "w500"
            in 501..780 -> "w780"
            else -> "original"
        }
        
        return Uri.parse(BASE_IMAGE_URL).buildUpon()
                .appendPath(sizeString)
                .appendEncodedPath(imageKey)
                .build()
    }
    
    @JvmStatic
    fun buildBackdropImageUri(applicationContext: Context, imagePath: String): Uri {
        val width = ImageWidthProvider.getMainBackdropWidth(applicationContext)
        
        return buildBackdropImageUri(width, imagePath)
    }
    
    @JvmStatic
    fun buildBackdropImageUri(width: Int, imagePath: String): Uri {
        check(width > 0) { "Invalid width: $width" }
        
        val sizeString = when(width) {
            in 1..300 -> "w300"
            in 301..780 -> "w780"
            in 781..1280 -> "w1280"
            else -> "original"
        }
        
        return Uri.parse(BASE_IMAGE_URL).buildUpon()
                .appendPath(sizeString)
                .appendEncodedPath(imagePath)
                .build()
    }
    
    @JvmStatic
    fun buildMovieUri(movieId: Int, application: Application): Uri {
        val language = (application as MoviesApp).injector.localeInfoProvider.language
        
        return buildMovieUri(movieId, language)
    }
    
    @JvmStatic
    fun buildMovieUri(movieId: Int, language: String): Uri =
            Uri.parse("https://www.themoviedb.org/movie/").buildUpon()
                    .appendPath(movieId.toString())
                    .appendQueryParameter("language", language)
                    .build()
}