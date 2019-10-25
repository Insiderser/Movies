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

package com.insiderser.android.movies.model.movie

import com.insiderser.android.movies.model.Cast
import com.insiderser.android.movies.model.Crew
import com.insiderser.android.movies.model.PosterDetails
import com.insiderser.android.movies.model.ReleaseDate
import com.insiderser.android.movies.model.Review
import com.insiderser.android.movies.model.Video

data class MovieDetails(
        override val id: Int,
        override val title: String,
        val overview: String,
        override val rating: Float,
        override val releaseYear: String,
        override val posterPath: String?,
        override val backdropPaths: List<String>,
        override val genres: List<String>,
        val tagline: String?,
        val runtime: Int,
        val budget: Long,
        val revenue: Long,
        val imdbId: String?,
        override val reviews: List<Review>,
        override val videos: List<Video>,
        override val recommendations: List<Movie>,
        override val similar: List<Movie>,
        val releaseDates: List<ReleaseDate>,
        val cast: List<Cast>,
        val crew: List<Crew>,
        override val isFullyCached: Boolean) : PosterDetails