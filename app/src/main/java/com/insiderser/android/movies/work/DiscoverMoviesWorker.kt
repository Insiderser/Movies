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

package com.insiderser.android.movies.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.insiderser.android.movies.MoviesApp
import com.insiderser.android.movies.model.types.toDiscoverType

class DiscoverMoviesWorker(context: Context, workerParams: WorkerParameters) :
        Worker(context, workerParams) {
    
    override fun doWork(): Result {
        val app = applicationContext as MoviesApp
        val injector = app.injector
        val discoverRepository = injector.discoverRepository
        val inputData = inputData
        val discoverType = inputData.getInt(INPUT_KEY_DISCOVER_TYPE, NO_TYPE_INPUT).let {
            if(it == NO_TYPE_INPUT) {
                throw IllegalArgumentException("DiscoverType must be passed as input data")
            } else it.toDiscoverType()
        }
        
        return discoverRepository.reload(discoverType)
    }
    
    companion object {
        
        @JvmField
        val BASE_TAG: String = DiscoverMoviesWorker::class.java.name
        
        const val INPUT_KEY_DISCOVER_TYPE = "discover_type"
        
        private const val NO_TYPE_INPUT = Int.MIN_VALUE
    }
}