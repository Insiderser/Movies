/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.utils.extentions

import com.insiderser.android.movies.model.Date
import com.insiderser.android.movies.model.Movie
import com.insiderser.android.movies.model.MovieSearchSuggestion
import com.insiderser.android.movies.model.PastQuerySearchSuggestion
import com.insiderser.android.movies.model.ReleaseDate
import com.insiderser.android.movies.model.Video
import com.insiderser.android.movies.model.entity.GenresEntity
import com.insiderser.android.movies.model.entity.MoviesEntity
import com.insiderser.android.movies.model.response.tmdb.TmdbGenre
import com.insiderser.android.movies.model.response.tmdb.TmdbMovie
import com.insiderser.android.movies.model.response.tmdb.TmdbMovieDetails
import com.insiderser.android.movies.model.response.tmdb.TmdbVideo
import com.insiderser.android.movies.model.toReleaseDateType
import com.insiderser.android.movies.model.toType

fun TmdbMovie.toMoviesEntity(): MoviesEntity {
    val backdropPaths = if(backdropPath != null) listOf(backdropPath) else emptyList()
    return MoviesEntity(id, title, overview, popularity, rating, getTmdbReleaseYear(releaseDate),
            posterPath,
            backdropPaths, genreIds)
}

fun TmdbMovie.toMovie() = Movie(id, title, posterPath)

fun Movie.toMovieSearchSuggestion() = MovieSearchSuggestion(id, title, posterPath)

fun String.toPastQuerySearchSuggestion() = PastQuerySearchSuggestion(this)

fun MoviesEntity.toMovie() = Movie(id, title, posterPath)

fun TmdbMovieDetails.toMoviesEntity(region: String, language: String): MoviesEntity {
    val releaseDates = releaseDates.results
            .find { it.region == region }
            ?.releaseDates
            ?.filter { it.language.isEmpty() || it.language == language }
            ?.mapNotNull {
                val releaseDateType = it.type.toReleaseDateType() ?: return@mapNotNull null
                val releaseDate = it.releaseDate.toTmdbReleaseDate() ?: return@mapNotNull null
                
                ReleaseDate(it.certification, releaseDate, releaseDateType)
            } ?: emptyList()
    
    return MoviesEntity(id, title, overview, popularity, rating, getTmdbReleaseYear(releaseDate),
            posterPath,
            images.backdrops.map { it.path }, genres.map { it.id }, tagline,
            runtime ?: MoviesEntity.DEFAULT_VALUE_RUNTIME, budget, revenue, imdbId,
            productionCompanies, reviews.reviews, videos.videos.map { it.toVideo() },
            recommendations.movies.map { it.id }, similarMovies.movies.map { it.id }, releaseDates,
            credits.cast, credits.crew, isMovieFullyCached = true)
}

fun TmdbGenre.toGenresEntity() = GenresEntity(id, name)

fun TmdbVideo.toVideo() = Video(imagePath, name, type.toType())

fun getTmdbReleaseYear(releaseDate: String?): Int = releaseDate?.toTmdbReleaseDate()?.year ?: 0

private fun String.toTmdbReleaseDate(): Date? {
    val parts = split(Regex("-"), limit = 3)
    
    if(parts.size < 3) return null
    
    val year = parts[0].toInt()
    val month = parts[1].toInt()
    val day = parts[2].substring(0..1).toInt()
    
    return Date(day, month, year)
}