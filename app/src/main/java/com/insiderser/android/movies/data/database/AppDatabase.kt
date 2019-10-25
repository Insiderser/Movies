/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.insiderser.android.movies.data.database.dao.DiscoverDao
import com.insiderser.android.movies.data.database.dao.FavouritesDao
import com.insiderser.android.movies.data.database.dao.GenresDao
import com.insiderser.android.movies.data.database.dao.MoviesDao
import com.insiderser.android.movies.data.database.dao.QueriesDao
import com.insiderser.android.movies.data.database.dao.TvShowsDao
import com.insiderser.android.movies.data.database.typeconverters.BackdropTypeConverter
import com.insiderser.android.movies.data.database.typeconverters.CastTypeConverter
import com.insiderser.android.movies.data.database.typeconverters.CrewTypeConverter
import com.insiderser.android.movies.data.database.typeconverters.DiscoverTypeTypeConverter
import com.insiderser.android.movies.data.database.typeconverters.ImageTypeConverter
import com.insiderser.android.movies.data.database.typeconverters.ListOfIntsTypeConverter
import com.insiderser.android.movies.data.database.typeconverters.ReleaseDatesTypeConverter
import com.insiderser.android.movies.data.database.typeconverters.ReviewsTypeConverter
import com.insiderser.android.movies.data.database.typeconverters.SeasonsTypeConverter
import com.insiderser.android.movies.data.database.typeconverters.TrailersTypeConverter
import com.insiderser.android.movies.data.database.typeconverters.TypeTypeConverter
import com.insiderser.android.movies.model.entity.DiscoverEntity
import com.insiderser.android.movies.model.entity.FavouritesEntity
import com.insiderser.android.movies.model.entity.GenresEntity
import com.insiderser.android.movies.model.entity.MoviesEntity
import com.insiderser.android.movies.model.entity.QueryEntity
import com.insiderser.android.movies.model.entity.TmdbImageConfigurationEntity
import com.insiderser.android.movies.model.entity.TvShowsEntity

@Database(entities = [FavouritesEntity::class, MoviesEntity::class, DiscoverEntity::class,
    GenresEntity::class, QueryEntity::class, TvShowsEntity::class, TmdbImageConfigurationEntity::class],
        version = AppDatabase.DATABASE_VERSION, exportSchema = false)
@TypeConverters(TrailersTypeConverter::class, ReviewsTypeConverter::class,
        BackdropTypeConverter::class, ListOfIntsTypeConverter::class, CastTypeConverter::class,
        ReleaseDatesTypeConverter::class, CrewTypeConverter::class, SeasonsTypeConverter::class,
        DiscoverTypeTypeConverter::class, TypeTypeConverter::class, ImageTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract val discoverDao: DiscoverDao
    
    abstract val moviesDao: MoviesDao
    
    abstract val tvShowsDao: TvShowsDao
    
    abstract val favouritesDao: FavouritesDao
    
    abstract val genresDao: GenresDao
    
    abstract val queriesDao: QueriesDao
    
    companion object {
        
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "movies"
    }
}