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

package com.insiderser.android.movies.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.insiderser.android.movies.model.Cast
import com.insiderser.android.movies.model.Crew
import com.insiderser.android.movies.model.Poster
import com.insiderser.android.movies.model.ReleaseDate
import com.insiderser.android.movies.model.Review
import com.insiderser.android.movies.model.Video

@Entity(tableName = "movies")
data class MoviesEntity(
        @PrimaryKey override val id: Int,
        override val title: String,
        val overview: String,
        val popularity: Float,
        val rating: Float,
        val releaseYear: Int,
        override val posterPath: String?,
        val backdropPaths: List<String>,
        val genreIds: List<Int>,
        val tagline: String? = null,
        val runtime: Int = DEFAULT_VALUE_RUNTIME,
        val budget: Long = 0L,
        val revenue: Long = 0L,
        val imdbId: String? = null,
        val reviews: List<Review> = emptyList(),
        val videos: List<Video> = emptyList(),
        val recommendationIds: List<Int> = emptyList(),
        val similarMoviesIds: List<Int> = emptyList(),
        val releaseDates: List<ReleaseDate> = emptyList(),
        val cast: List<Cast> = emptyList(),
        val crew: List<Crew> = emptyList(),
        val isMovieFullyCached: Boolean = false) : Poster {
    
    companion object {
        const val DEFAULT_VALUE_RUNTIME = 0
    }
}