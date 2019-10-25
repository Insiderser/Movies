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

package com.insiderser.android.movies.di

import android.app.Activity
import androidx.fragment.app.Fragment
import com.insiderser.android.movies.MoviesApp
import com.insiderser.android.movies.api.tmdb.TmdbUriBuilder
import com.insiderser.android.movies.data.database.AppDatabase
import com.insiderser.android.movies.data.preferences.PreferencesHelper
import com.insiderser.android.movies.data.repository.DiscoverRepository
import com.insiderser.android.movies.data.repository.GenresRepository
import com.insiderser.android.movies.ui.details.basic.BasicDetailsViewModel
import com.insiderser.android.movies.ui.details.full.FullMovieDetailsViewModel
import com.insiderser.android.movies.ui.main.DiscoverViewModel
import com.insiderser.android.movies.ui.main.FavouritesViewModel
import com.insiderser.android.movies.ui.main.MainViewModel
import com.insiderser.android.movies.ui.main.SearchViewModel
import com.insiderser.android.movies.ui.main.SortByDialogViewModel
import com.insiderser.android.movies.ui.recommendations.RecommendationsViewModel
import com.insiderser.android.movies.ui.reviews.ReviewsViewModel
import com.insiderser.android.movies.utils.ImageWidthProvider
import com.insiderser.android.movies.utils.system.LocaleInfoProvider
import com.insiderser.android.movies.utils.system.NetworkInfoProvider
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ContextModule::class, DatabaseModule::class, APIModule::class])
interface SingletonComponent {
    
    fun getMainViewModel(): MainViewModel
    
    fun getDiscoverViewModel(): DiscoverViewModel
    
    fun getFavouritesViewModel(): FavouritesViewModel
    
    fun getSearchViewModel(): SearchViewModel
    
    fun getSortByViewModel(): SortByDialogViewModel
    
    fun getBasicMovieDetailsViewModel(): BasicDetailsViewModel
    
    fun getFullMovieDetailsViewModel(): FullMovieDetailsViewModel
    
    fun getRecommendationsViewModel(): RecommendationsViewModel
    
    fun getReviewsViewModel(): ReviewsViewModel
    
    val database: AppDatabase
    
    val localeInfoProvider: LocaleInfoProvider
    
    val networkInfoProvider: NetworkInfoProvider
    
    val preferencesHelper: PreferencesHelper
    
    val discoverRepository: DiscoverRepository
    
    val genresRepository: GenresRepository
    
    val imageWidthProvider: ImageWidthProvider
    
    val tmdbUriBuilder: TmdbUriBuilder
}

val Activity.injector get() = (application as MoviesApp).injector

val Fragment.injector get() = (requireActivity().application as MoviesApp).injector