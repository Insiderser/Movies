/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.data.repository

import com.insiderser.android.movies.data.database.AppDatabase
import com.insiderser.android.movies.model.entity.FavouritesEntity
import com.insiderser.android.movies.model.types.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouritesRepository @Inject constructor(private val database: AppDatabase) {
    
    fun getFavourites() = database.favouritesDao.getFavourites()
    
    fun isInFavourites(id: Int) =
            database.favouritesDao.isInFavourites(id)
    
    fun addToFavourites(id: Int, type: Type) {
        GlobalScope.launch(Dispatchers.IO) {
            database.favouritesDao.addToFavourites(FavouritesEntity(id, type))
        }
    }
    
    fun deleteFromFavourites(movieId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            database.favouritesDao.removeFromFavourites(movieId)
        }
    }
}