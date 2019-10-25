/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.ui.recommendations

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.insiderser.android.movies.R
import com.insiderser.android.movies.model.types.MoviesSuggestionsType
import com.insiderser.android.movies.model.types.toInt
import com.insiderser.android.movies.model.types.toMoviesType
import com.insiderser.android.movies.utils.extentions.inTransaction
import kotlinx.android.synthetic.main.activity_recommendations.*

class RecommendationsActivity : AppCompatActivity() {
    
    companion object {
        
        const val EXTRA_MOVIE_ID = "movie_id"
        const val EXTRA_TYPE = "type"
        
        @JvmStatic
        fun buildIntent(context: Context, movieId: Int, type: MoviesSuggestionsType): Intent =
                Intent(context, RecommendationsActivity::class.java)
                        .putExtra(
                                EXTRA_MOVIE_ID, movieId)
                        .putExtra(
                                EXTRA_TYPE, type.toInt())
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_recommendations)
        
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        val intent = intent
        
        check(intent.hasExtra(EXTRA_MOVIE_ID)) { "Movie ID must be passed as intent extra" }
        check(intent.hasExtra(EXTRA_TYPE)) { "Type must be passed as intent extra" }
        
        val movieId = intent.getIntExtra(EXTRA_MOVIE_ID, 0)
        val type = intent.getIntExtra(EXTRA_TYPE, 0).toMoviesType()
        
        val titleRes = when(type) {
            MoviesSuggestionsType.RECOMMENDATIONS -> R.string.recommended
            MoviesSuggestionsType.SIMILAR_MOVIES -> R.string.similar_movies
        }
        
        setTitle(titleRes)
        
        if(savedInstanceState == null) {
            val moviesFragment = RecommendationsFragment.newInstance(movieId, type)
            
            supportFragmentManager.inTransaction {
                add(R.id.fragment_container, moviesFragment, RecommendationsFragment.TAG)
            }
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}