/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.model.types

import com.insiderser.android.movies.data.preferences.PreferencesHelper
import com.insiderser.android.movies.model.sort_by.MovieSortBy
import com.insiderser.android.movies.model.sort_by.TvShowSortBy

enum class DiscoverType {
    MOVIE_POPULARITY, MOVIE_VOTE_COUNT, MOVIE_REVENUE,
    TV_SHOW_POPULARITY, TV_SHOW_VOTE_AVERAGE
}

private const val MOVIE_POPULARITY = 0
private const val MOVIE_VOTE_COUNT = 1
private const val MOVIE_REVENUE = 2
private const val TV_SHOW_POPULARITY = 3
private const val TV_SHOW_VOTE_AVERAGE = 4

fun DiscoverType.toInt() = when(this) {
    DiscoverType.MOVIE_POPULARITY -> MOVIE_POPULARITY
    DiscoverType.MOVIE_VOTE_COUNT -> MOVIE_VOTE_COUNT
    DiscoverType.MOVIE_REVENUE -> MOVIE_REVENUE
    DiscoverType.TV_SHOW_POPULARITY -> TV_SHOW_POPULARITY
    DiscoverType.TV_SHOW_VOTE_AVERAGE -> TV_SHOW_VOTE_AVERAGE
}

fun Int.toDiscoverType() = when(this) {
    MOVIE_POPULARITY -> DiscoverType.MOVIE_POPULARITY
    MOVIE_VOTE_COUNT -> DiscoverType.MOVIE_VOTE_COUNT
    MOVIE_REVENUE -> DiscoverType.MOVIE_REVENUE
    TV_SHOW_POPULARITY -> DiscoverType.TV_SHOW_POPULARITY
    TV_SHOW_VOTE_AVERAGE -> DiscoverType.TV_SHOW_VOTE_AVERAGE
    else -> throw IllegalArgumentException("Cannot convert $this to DiscoverType")
}

@Suppress("FunctionName")
fun DiscoverType(type: Type, preferencesHelper: PreferencesHelper) = when(type) {
    Type.MOVIE -> when(preferencesHelper.movieSortBy) {
        MovieSortBy.POPULARITY -> DiscoverType.MOVIE_POPULARITY
        MovieSortBy.VOTE_COUNT -> DiscoverType.MOVIE_VOTE_COUNT
        MovieSortBy.REVENUE -> DiscoverType.MOVIE_REVENUE
    }
    Type.TV_SHOW -> when(preferencesHelper.tvShowSortBy) {
        TvShowSortBy.POPULARITY -> DiscoverType.TV_SHOW_POPULARITY
        TvShowSortBy.VOTE_AVERAGE -> DiscoverType.TV_SHOW_VOTE_AVERAGE
    }
}

fun DiscoverType.toType() = when(this) {
    DiscoverType.MOVIE_POPULARITY, DiscoverType.MOVIE_VOTE_COUNT,
    DiscoverType.MOVIE_REVENUE -> Type.MOVIE
    DiscoverType.TV_SHOW_POPULARITY, DiscoverType.TV_SHOW_VOTE_AVERAGE -> Type.TV_SHOW
}