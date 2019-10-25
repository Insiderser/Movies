/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.data.preferences

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.annotation.WorkerThread
import com.insiderser.android.movies.model.SortBy
import com.insiderser.android.movies.model.toInt
import com.insiderser.android.movies.model.toSortBy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesHelper @Inject constructor(application: Application) {
    
    private val sharedPreferences: SharedPreferences =
            application.getSharedPreferences(MOVIES_PREFERENCES, Context.MODE_PRIVATE)
    
    var sortBy: SortBy
        @WorkerThread get() =
            if(sharedPreferences.contains(PREFERENCE_SORT_BY_KEY))
                sharedPreferences.getInt(PREFERENCE_SORT_BY_KEY, - 1).toSortBy()
            else
                SortBy.POPULARITY
        set(value) {
            sharedPreferences.edit()
                    .putInt(PREFERENCE_SORT_BY_KEY, value.toInt())
                    .apply()
        }
    
    fun registerOnPreferenceChangedListener(listener: OnSharedPreferenceChangeListener) =
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    
    companion object {
        private const val MOVIES_PREFERENCES = "movies"
        
        private const val PREFERENCE_SORT_BY_KEY = "sort_by"
        
        @JvmStatic
        fun isPreferenceKeySortBy(key: String) = key == PREFERENCE_SORT_BY_KEY
    }
}