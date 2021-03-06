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

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.insiderser.android.movies.model.entity.DiscoverEntity
import com.insiderser.android.movies.model.movie.Movie
import com.insiderser.android.movies.model.tv.TvShow
import com.insiderser.android.movies.model.types.DiscoverType

@Dao
interface DiscoverDao {
    
    @Query("""
        SELECT discover.id, movies.title, movies.posterPath
        FROM discover
        INNER JOIN movies ON discover.id = movies.id
        WHERE discover.type = :type
        ORDER BY discover.positionInList ASC
        """)
    fun getMovies(type: DiscoverType): LiveData<List<Movie>>
    
    @Query("""
        SELECT discover.id, tv.title, tv.posterPath
        FROM discover
        INNER JOIN tv_shows "tv" ON discover.id = tv.id
        WHERE discover.type = :type
        ORDER BY discover.positionInList ASC
    """)
    fun getTvShows(type: DiscoverType): LiveData<List<TvShow>>
    
    @Insert(onConflict = REPLACE)
    fun insertAll(movies: List<DiscoverEntity>)
    
    @Query("DELETE FROM discover WHERE type = :type")
    fun deleteAll(type: DiscoverType)
}