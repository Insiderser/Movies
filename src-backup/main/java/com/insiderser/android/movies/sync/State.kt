/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.sync

import android.content.SharedPreferences
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.insiderser.android.movies.MoviesApp
import com.insiderser.android.movies.data.database.AppDatabase
import com.insiderser.android.movies.data.preferences.PreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object State {
    
    private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener
    
    @JvmStatic
    fun init(app: MoviesApp) {
        val injector = app.injector
        val database = injector.database
        val preferencesHelper = injector.preferencesHelper
        
        syncState(database)
        
        listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            GlobalScope.launch(Dispatchers.IO) {
                if(PreferencesHelper.isPreferenceKeySortBy(key)) {
                    database.discoverDao.deleteAll()
                }
            }
        }
        
        preferencesHelper.registerOnPreferenceChangedListener(listener)
    }
    
    @JvmStatic
    private fun syncState(database: AppDatabase) {
        GlobalScope.launch(Dispatchers.IO) {
            if(database.genresDao.getCount() == 0) {
                syncGenres()
            }
        }
    }
    
    @JvmStatic
    fun syncGenres() {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        
        val request = OneTimeWorkRequestBuilder<ReloadGenresWorker>()
                .setConstraints(constraints)
                .addTag(ReloadGenresWorker.TAG)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
        
        WorkManager.getInstance()
                .beginUniqueWork(ReloadGenresWorker.TAG, ExistingWorkPolicy.KEEP, request)
                .enqueue()
    }
}