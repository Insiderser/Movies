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

package com.insiderser.android.movies.model.response.tmdb.tv

import com.google.gson.annotations.SerializedName
import com.insiderser.android.movies.model.response.tmdb.TmdbCredits
import com.insiderser.android.movies.model.response.tmdb.TmdbExternalIds
import com.insiderser.android.movies.model.response.tmdb.TmdbReviews
import com.insiderser.android.movies.model.response.tmdb.genre.TmdbGenre
import com.insiderser.android.movies.model.response.tmdb.images.TmdbImages
import com.insiderser.android.movies.model.response.tmdb.videos.TmdbVideos

data class TmdbTvShowDetails(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String,
        @SerializedName("overview") val overview: String,
        @SerializedName("poster_path") val posterPath: String?,
        @SerializedName("popularity") val popularity: Float,
        @SerializedName("vote_average") val rating: Float,
        @SerializedName("genres") val genres: List<TmdbGenre>,
        @SerializedName("reviews") val reviews: TmdbReviews,
        @SerializedName("videos") val videos: TmdbVideos,
        @SerializedName("images") val images: TmdbImages,
        @SerializedName("recommendations") val recommendations: TmdbTvShows,
        @SerializedName("similar") val similarTvShows: TmdbTvShows,
        @SerializedName("credits") val credits: TmdbCredits,
        @SerializedName("external_ids") val externalIds: TmdbExternalIds,
        @SerializedName("seasons") val seasons: List<TmdbTvSeason>)