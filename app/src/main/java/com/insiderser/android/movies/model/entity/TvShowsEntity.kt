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
import com.insiderser.android.movies.model.Review
import com.insiderser.android.movies.model.Video
import com.insiderser.android.movies.model.tv.TvSeason

@Entity(tableName = "tv_shows")
data class TvShowsEntity(
        @PrimaryKey override val id: Int,
        override val title: String,
        val overview: String,
        val popularity: Float,
        val rating: Float,
        override val posterPath: String?,
        val genreIds: List<Int>,
        val imdbId: String? = null,
        val backdropPaths: List<String> = emptyList(),
        val reviews: List<Review> = emptyList(),
        val videos: List<Video> = emptyList(),
        val recommendationIds: List<Int> = emptyList(),
        val similarTvShowIds: List<Int> = emptyList(),
        val seasons: List<TvSeason> = emptyList(),
        val cast: List<Cast> = emptyList(),
        val crew: List<Crew> = emptyList(),
        val isFullyCached: Boolean = false) : Poster