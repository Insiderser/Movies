/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.api.tmdb

import android.net.Uri
import com.insiderser.android.movies.model.types.ImageType
import com.insiderser.android.movies.utils.ImageWidthProvider
import com.insiderser.android.movies.utils.system.LocaleInfoProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbUriBuilder @Inject constructor(
        private val localeInfoProvider: LocaleInfoProvider,
        private val imageWidthProvider: ImageWidthProvider) {
    
    fun buildPosterImageUri(imageKey: String,
            width: Int = imageWidthProvider.getPosterWidth(),
            height: Int = width * 3 / 2): Uri =
            Uri.parse(TmdbImageConfiguration.BASE_IMAGE_URL).buildUpon()
                    .appendPath(TmdbImageConfiguration.findSuitableSizeUrlPath(ImageType.POSTER,
                            width, height))
                    .appendEncodedPath(imageKey)
                    .build()
    
    fun buildBackdropImageUri(imagePath: String,
            width: Int = imageWidthProvider.getMainBackdropWidth(),
            height: Int = width * 9 / 16): Uri =
            Uri.parse(TmdbImageConfiguration.BASE_IMAGE_URL).buildUpon()
                    .appendPath(TmdbImageConfiguration.findSuitableSizeUrlPath(ImageType.POSTER,
                            width, height))
                    .appendEncodedPath(imagePath)
                    .build()
    
    fun buildMovieUri(movieId: Int): Uri =
            Uri.parse("https://www.themoviedb.org/movie/").buildUpon()
                    .appendPath(movieId.toString())
                    .appendQueryParameter("language", localeInfoProvider.language)
                    .build()
}