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
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.insiderser.android.movies.model.entity.GenresEntity

/**
 * <b>NOTE</b>: here are stored both TV and Movies genres.
 */
@Dao
abstract class GenresDao {
    
    @Query("SELECT * FROM genres")
    abstract fun getGenres(): LiveData<List<GenresEntity>>
    
    @Transaction
    open fun insertOrUpdate(genres: List<GenresEntity>) {
        genres.forEach { genre ->
            val rowsUpdated = update(genre)
            if(rowsUpdated == 0) {
                insert(genre)
            }
        }
    }
    
    @Update(onConflict = IGNORE)
    protected abstract fun update(genre: GenresEntity): Int
    
    @Insert
    protected abstract fun insert(genre: GenresEntity)
}