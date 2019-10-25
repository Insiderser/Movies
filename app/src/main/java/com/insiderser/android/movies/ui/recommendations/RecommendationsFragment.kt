/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.ui.recommendations

import android.os.Bundle
import androidx.annotation.LayoutRes
import com.insiderser.android.movies.di.injector
import com.insiderser.android.movies.model.types.MoviesSuggestionsType
import com.insiderser.android.movies.model.types.toInt
import com.insiderser.android.movies.model.types.toMoviesType
import com.insiderser.android.movies.ui.PosterListFragment
import com.insiderser.android.movies.utils.extentions.viewModelProvider

class RecommendationsFragment : PosterListFragment() {
    
    override val source: RecommendationsViewModel by viewModelProvider {
        injector.getRecommendationsViewModel().also { viewModel ->
            val arguments = arguments
            
            check(arguments != null && arguments.containsKey(ARGUMENT_MOVIE_ID)
                    && arguments.containsKey(ARGUMENT_TYPE)) {
                "Did you create RecommendationsFragment using its primary constructor? " +
                        "Use RecommendationsFragment.newInstance() instead."
            }
            
            val movieId = arguments.getInt(ARGUMENT_MOVIE_ID)
            val type = arguments.getInt(ARGUMENT_TYPE).toMoviesType()
            
            viewModel.initState(movieId, type)
        }
    }
    
    @LayoutRes
    override val emptyLayoutRes: Int = 0
    
    init {
        shouldSwipeRefrLayoutBeEnabled = false
    }
    
    companion object {
        
        @JvmField
        val TAG: String = RecommendationsFragment::class.java.name
        
        const val ARGUMENT_MOVIE_ID = "movie_id"
        const val ARGUMENT_TYPE = "type"
        
        @JvmStatic
        fun newInstance(movieId: Int, type: MoviesSuggestionsType) =
                RecommendationsFragment().apply {
                    arguments = Bundle(2).also { arguments ->
                        arguments.putInt(ARGUMENT_MOVIE_ID, movieId)
                        arguments.putInt(ARGUMENT_TYPE, type.toInt())
                    }
                }
    }
}