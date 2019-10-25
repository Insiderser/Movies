/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import com.insiderser.android.movies.model.Poster
import com.insiderser.android.movies.model.entity.FavouritesEntity
import com.insiderser.android.movies.model.movie.Movie
import com.insiderser.android.movies.model.tv.TvShow
import com.insiderser.android.movies.model.types.Type
import com.insiderser.android.movies.utils.extentions.map

@Dao
abstract class FavouritesDao {
    
    fun getFavourites(): LiveData<List<Poster>> = getFavouritesInternal().map { rawFavourites ->
        rawFavourites?.map { it.toPoster() }
    }
    
    @Query("""
        SELECT favourites.id AS "id", movies.title AS "title", movies.posterPath AS "posterPath",
               favourites.type AS "type", favourites.timeAdded AS "timeAdded"
        FROM favourites
        INNER JOIN movies ON favourites.id = movies.id
        
        UNION ALL
        
        SELECT favourites.id AS "id", tv.title AS "title", tv.posterPath AS "posterPath",
               favourites.type AS "type", favourites.timeAdded AS "timeAdded"
        FROM favourites
        INNER JOIN tv_shows "tv" ON favourites.id = tv.id

        ORDER BY timeAdded ASC
        """)
    protected abstract fun getFavouritesInternal(): LiveData<List<Favourite>>
    
    @Query("SELECT COUNT(1) FROM favourites WHERE id = :id")
    abstract fun isInFavourites(id: Int): LiveData<Boolean>
    
    @Insert(onConflict = IGNORE)
    abstract fun addToFavourites(favouritesEntity: FavouritesEntity)
    
    @Query("DELETE FROM favourites WHERE id = :id")
    abstract fun removeFromFavourites(id: Int)
    
    protected data class Favourite(
            override val id: Int,
            override val title: String,
            override val posterPath: String?,
            val type: Type,
            val timeAdded: Long) : Poster
    
    private fun Favourite.toPoster() = when(type) {
        Type.MOVIE -> Movie(id, title, posterPath)
        Type.TV_SHOW -> TvShow(id, title, posterPath)
    }
}