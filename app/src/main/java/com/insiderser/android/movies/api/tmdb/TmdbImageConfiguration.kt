/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.api.tmdb

import com.insiderser.android.movies.model.types.ImageType

object TmdbImageConfiguration {
    
    const val BASE_IMAGE_URL = "https://image.tmdb.org/t/p/"
    
    @JvmStatic
    fun findSuitableSizeUrlPath(imageType: ImageType, imageWidth: Int, imageHeight: Int): String {
        check(imageWidth > 0) { "Invalid width: $imageWidth" }
        check(imageHeight > 0) { "Invalid height: $imageHeight" }
        
        return when(imageType) {
            ImageType.POSTER -> when(imageWidth) {
                in 1..92 -> "w92"
                in 93..154 -> "w154"
                in 155..185 -> "w185"
                in 186..342 -> "w342"
                in 343..500 -> "w500"
                in 501..780 -> "w780"
                else -> "original"
            }
            ImageType.BACKDROP -> when(imageWidth) {
                in 1..300 -> "w300"
                in 301..780 -> "w780"
                in 781..1280 -> "w1280"
                else -> "original"
            }
            ImageType.LOGO -> when(imageWidth) {
                in 1..45 -> "w45"
                in 46..92 -> "w92"
                in 93..154 -> "w154"
                in 155..185 -> "w185"
                in 186..300 -> "w300"
                in 301..500 -> "w500"
                else -> "original"
            }
            ImageType.PROFILE -> when {
                imageWidth < 45 -> "w45"
                imageWidth < 185 -> "w185"
                imageHeight < 632 -> "h632"
                else -> "original"
            }
            ImageType.STILL -> when(imageWidth) {
                in 1..92 -> "w92"
                in 93..185 -> "w185"
                in 186..300 -> "w300"
                else -> "original"
            }
        }
    }
}