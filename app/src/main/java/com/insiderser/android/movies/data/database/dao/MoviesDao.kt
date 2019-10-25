/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.data.database.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.insiderser.android.movies.data.database.typeconverters.ListOfIntsTypeConverter
import com.insiderser.android.movies.model.entity.MoviesEntity
import com.insiderser.android.movies.model.movie.Movie
import com.insiderser.android.movies.model.response.tmdb.movie.TmdbMovie
import com.insiderser.android.movies.utils.extentions.getTmdbReleaseYear
import com.insiderser.android.movies.utils.extentions.toMoviesEntity

@Dao
abstract class MoviesDao {
    
    @Query("""
        SELECT *
        FROM movies
        WHERE id = :id
        """)
    abstract fun getMovie(id: Int): LiveData<MoviesEntity>
    
    @WorkerThread
    fun getMovies(ids: List<Int>): List<Movie> {
        val foundMovies = getMoviesInternal(ids).associateBy { it.id }
        return ids.mapNotNull { foundMovies[it] }
    }
    
    @WorkerThread
    @Query("""
        SELECT id, title, posterPath
        FROM movies
        WHERE id IN (:ids)
        """)
    protected abstract fun getMoviesInternal(ids: List<Int>): List<Movie>
    
    @WorkerThread
    @Query("""
        SELECT * FROM movies
        WHERE title LIKE '%' || :query || '%'
        ORDER BY popularity DESC
        """)
    abstract fun searchMovies(query: String): List<MoviesEntity>
    
    @WorkerThread
    @Update
    abstract fun update(entity: MoviesEntity)
    
    @WorkerThread
    @Transaction
    open fun insertOrUpdate(movies: List<TmdbMovie>) {
        movies.forEach { movie ->
            val convertedGenreIds = ListOfIntsTypeConverter.fromList(movie.genreIds)
            val releaseYear = getTmdbReleaseYear(movie.releaseDate)
            
            val rowsUpdated = update(movie.id, movie.title, movie.overview, movie.popularity,
                    movie.rating, releaseYear, movie.posterPath, convertedGenreIds)
            
            if(rowsUpdated == 0) {
                insert(movie.toMoviesEntity())
            }
        }
    }
    
    @WorkerThread
    @Insert
    protected abstract fun insert(entity: MoviesEntity)
    
    @WorkerThread
    @Query("""
        UPDATE movies
        SET title = :title, overview = :overview, popularity = :popularity, rating = :rating,
        releaseYear = :releaseYear, posterPath = :posterPath, genreIds = :convertedGenreIds
        WHERE id = :id
        """)
    protected abstract fun update(id: Int, title: String, overview: String, popularity: Float,
            rating: Float, releaseYear: Int, posterPath: String?, convertedGenreIds: String?): Int
}