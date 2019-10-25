/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.model.response.tmdb

import com.google.gson.annotations.SerializedName

data class TmdbReleaseDates(
        @SerializedName("results") val results: List<TmdbReleaseDateWrapper>)

data class TmdbReleaseDateWrapper(
        @SerializedName("iso_3166_1") val region: String,
        @SerializedName("release_dates") val releaseDates: List<TmdbReleaseDate>)

data class TmdbReleaseDate(
        @SerializedName("certification") val certification: String,
        @SerializedName("iso_639_1") val language: String,
        @SerializedName("release_date") val releaseDate: String,
        @SerializedName("type") val type: Int)