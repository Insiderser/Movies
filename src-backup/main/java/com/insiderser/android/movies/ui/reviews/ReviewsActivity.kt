/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.ui.reviews

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.insiderser.android.movies.R
import com.insiderser.android.movies.databinding.ActivityReviewsBinding
import com.insiderser.android.movies.di.injector
import com.insiderser.android.movies.ui.ReviewsAdapter
import com.insiderser.android.movies.utils.extentions.lazyUnsynchronized
import com.insiderser.android.movies.utils.extentions.viewModelProvider

class ReviewsActivity : AppCompatActivity() {
    
    companion object {
        
        const val EXTRA_MOVIE_ID = "movie_id"
        const val EXTRA_POSITION = "position"
        
        const val NO_POSITION = - 1
        
        @JvmStatic
        fun buildIntent(context: Context, movieId: Int, position: Int = NO_POSITION): Intent =
                Intent(context, ReviewsActivity::class.java).apply {
                    putExtra(EXTRA_MOVIE_ID, movieId)
                    
                    if(position != NO_POSITION) {
                        putExtra(EXTRA_POSITION, position)
                    }
                }
    }
    
    private lateinit var binding: ActivityReviewsBinding
    
    private val viewModel: ReviewsViewModel by viewModelProvider {
        injector.getDetailsReviewsViewModel().also { viewModel ->
            val intent = intent
            
            check(intent.hasExtra(EXTRA_MOVIE_ID)) { "Movie ID must be passed as intent extra" }
            
            val movieId = intent.getIntExtra(EXTRA_MOVIE_ID, 0)
            
            viewModel.initState(movieId)
        }
    }
    
    private val reviewsAdapter by lazyUnsynchronized { ReviewsAdapter() }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reviews)
        
        val intent = intent
        
        binding.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            
            lifecycleOwner = this@ReviewsActivity
            isRefreshing = viewModel.isRefreshing
            
            reviewsList.adapter = reviewsAdapter
            
            if(savedInstanceState == null && intent.hasExtra(EXTRA_POSITION)) {
                val position = intent.getIntExtra(EXTRA_POSITION, 0)
                
                // Issue: https://stackoverflow.com/questions/36426129/recyclerview-scroll-to-position-not-working-every-time
                Handler().postDelayed({
                    reviewsList.scrollToPosition(position)
                }, 300)
            }
            
            swipeRefrLayout.setColorSchemeResources(android.R.color.holo_red_dark,
                    android.R.color.holo_green_dark,
                    android.R.color.holo_blue_dark,
                    android.R.color.holo_orange_dark)
            
            swipeRefrLayout.setOnRefreshListener {
                reload()
            }
        }
        
        viewModel.reviews.observe(this, Observer(reviewsAdapter::submitList))
    }
    
    private fun reload() {
        viewModel.reloadReviews()
        
        val isNetworkAvailable = injector.networkInfoProvider.isNetworkAvailable
        
        if(! isNetworkAvailable) {
            Snackbar.make(binding.coordinatorLayout, R.string.no_internet, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) {
                        reload()
                    }
                    .show()
            
            binding.swipeRefrLayout.isRefreshing = false
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when(item?.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}