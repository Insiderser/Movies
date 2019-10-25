/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.utils

import android.app.Application
import com.insiderser.android.movies.R
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class ImageWidthProvider @Inject constructor(private val application: Application) {
    
    companion object {
        private const val NOT_CALCULATED = Int.MIN_VALUE
    }
    
    private var poster: Int = NOT_CALCULATED
    private var mainBackdrop: Int = NOT_CALCULATED
    private var detailsImages: Int = NOT_CALCULATED
    
    private val lock = Any()
    
    fun getPosterWidth(): Int {
        poster.ifNotCalculated {
            val resources = application.resources
            val displayMetrics = resources.displayMetrics
            
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            
            val widthPortrait = min(screenHeight, screenWidth)
            val widthLandscape = max(screenHeight, screenWidth)
            
            val spanCountPortrait =
                    resources.getInteger(
                            R.integer.movie_list_span_count_portrait)
            val spanCountLandscape =
                    resources.getInteger(
                            R.integer.movie_list_span_count_landscape)
            
            val maxWidthMainList = max(widthPortrait / spanCountPortrait,
                    widthLandscape / spanCountLandscape)
            
            val movieSuggestionWidth = resources.getDimensionPixelSize(
                    R.dimen.list_item_movie_suggestion_image_width)
            
            poster = max(maxWidthMainList, movieSuggestionWidth)
        }
        
        return poster
    }
    
    fun getMainBackdropWidth(): Int {
        mainBackdrop.ifNotCalculated {
            val displayMetrics = application.resources.displayMetrics
            
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            
            mainBackdrop = max(screenHeight, screenWidth)
        }
        
        return mainBackdrop
    }
    
    fun getMovieDetailsImagesWidth(): Int {
        detailsImages.ifNotCalculated {
            detailsImages = application.resources.getDimensionPixelSize(R.dimen.images_item_width)
        }
        
        return detailsImages
    }
    
    private inline fun Int.ifNotCalculated(onCalculate: () -> Unit) {
        if(this == NOT_CALCULATED) {
            synchronized(lock) {
                if(this == NOT_CALCULATED) {
                    onCalculate()
                }
            }
        }
    }
}