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

package com.insiderser.android.movies.data.database.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.insiderser.android.movies.data.database.typeconverters.ListOfIntsTypeConverter
import com.insiderser.android.movies.model.entity.TvShowsEntity
import com.insiderser.android.movies.model.response.tmdb.tv.TmdbTvShow
import com.insiderser.android.movies.model.tv.TvShow
import com.insiderser.android.movies.utils.extentions.toTvShowEntity

@Dao
abstract class TvShowsDao {
    
    @Query("""
        SELECT *
        FROM tv_shows
        WHERE id = :id
        """)
    abstract fun getShow(id: Int): LiveData<TvShowsEntity>
    
    @WorkerThread
    fun getShows(ids: List<Int>): List<TvShow> {
        val foundMovies = getShowsInternal(ids).associateBy { it.id }
        return ids.mapNotNull { foundMovies[it] }
    }
    
    @WorkerThread
    @Query("""
        SELECT id, title, posterPath
        FROM tv_shows
        WHERE id IN (:ids)
        """)
    protected abstract fun getShowsInternal(ids: List<Int>): List<TvShow>
    
    @WorkerThread
    @Query("""
        SELECT * FROM tv_shows
        WHERE title LIKE '%' || :query || '%'
        ORDER BY popularity DESC
        """)
    abstract fun searchShows(query: String): List<TvShowsEntity>
    
    @WorkerThread
    @Update
    abstract fun update(entity: TvShowsEntity)
    
    @WorkerThread
    @Transaction
    open fun insertOrUpdate(movies: List<TmdbTvShow>) {
        movies.forEach { movie ->
            val convertedGenreIds = ListOfIntsTypeConverter.fromList(movie.genreIds)
            
            val rowsUpdated = update(movie.id, movie.name, movie.overview, movie.popularity,
                    movie.rating, movie.posterPath, convertedGenreIds)
            
            if(rowsUpdated == 0) {
                insert(movie.toTvShowEntity())
            }
        }
    }
    
    @WorkerThread
    @Insert
    protected abstract fun insert(entity: TvShowsEntity)
    
    @WorkerThread
    @Query("""
        UPDATE tv_shows
        SET title = :name, overview = :overview, popularity = :popularity, rating = :rating,
        posterPath = :posterPath, genreIds = :convertedGenreIds
        WHERE id = :id
        """)
    protected abstract fun update(id: Int, name: String, overview: String, popularity: Float,
            rating: Float, posterPath: String?, convertedGenreIds: String?): Int
}