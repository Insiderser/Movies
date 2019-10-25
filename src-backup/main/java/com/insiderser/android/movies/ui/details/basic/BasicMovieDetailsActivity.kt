/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.ui.details.basic

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.insiderser.android.movies.R
import com.insiderser.android.movies.api.tmdb.TmdbUriBuilder
import com.insiderser.android.movies.api.youtube.YouTubeUriBuilder
import com.insiderser.android.movies.databinding.ActivityDetailsBasicBinding
import com.insiderser.android.movies.di.injector
import com.insiderser.android.movies.glide.GlideApp
import com.insiderser.android.movies.glide.GlideRequest
import com.insiderser.android.movies.glide.GlideUtils
import com.insiderser.android.movies.model.MovieDetails
import com.insiderser.android.movies.model.Video
import com.insiderser.android.movies.ui.ReviewsAdapter
import com.insiderser.android.movies.ui.details.full.FullMovieDetailsActivity
import com.insiderser.android.movies.ui.movies.MoviesActivity
import com.insiderser.android.movies.ui.movies.MoviesType
import com.insiderser.android.movies.ui.reviews.ReviewsActivity
import com.insiderser.android.movies.utils.extentions.lazyUnsynchronized
import com.insiderser.android.movies.utils.extentions.viewModelProvider
import com.stfalcon.imageviewer.StfalconImageViewer

class BasicMovieDetailsActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_MOVIE_ID = "id"
        
        @JvmStatic
        fun buildIntent(context: Context, movieId: Int): Intent =
                Intent(context, BasicMovieDetailsActivity::class.java)
                        .putExtra(EXTRA_MOVIE_ID, movieId)
        
        private const val KEY_IMAGE_POSITION_SHOWING_FULLSCREEN = "image position showing fullscreen"
    }
    
    private val viewModel: BasicMovieDetailsViewModel by viewModelProvider {
        val intent = intent
        
        check(intent.hasExtra(EXTRA_MOVIE_ID)) { "Movie ID must be passed as intent extra" }
        
        val movieId = intent.getIntExtra(EXTRA_MOVIE_ID, - 1)
        injector.getDetailsViewModel().also { viewModel ->
            viewModel.initState(movieId)
        }
    }
    
    private lateinit var binding: ActivityDetailsBasicBinding
    
    private val imagesAdapter by lazyUnsynchronized {
        MovieImagesAdapter(this,
                onVideoClickCallback = this::openTrailer,
                onMovieImageClickCallback = this::showImageFullscreen)
    }
    
    private val recommendationsAdapter by lazyUnsynchronized {
        MovieRecommendationsAdapter(this, onShowMoreClickCallback = {
            showDetailsMovies(MoviesType.RECOMMENDATIONS)
        })
    }
    
    private val similarMoviesAdapter by lazyUnsynchronized {
        MovieRecommendationsAdapter(this, onShowMoreClickCallback = {
            showDetailsMovies(MoviesType.SIMILAR_MOVIES)
        })
    }
    
    private val reviewsAdapter by lazyUnsynchronized {
        ReviewsAdapter(maxReviewsCount = 2,
                onItemClickCallback = { position ->
                    showAllReviews(position)
                })
    }
    
    private val movieDetailsObserver = Observer { movieDetails: MovieDetails? ->
        updateContentVisibility(movieDetails)
        
        loadPoster(movieDetails)
        loadBackdrop(movieDetails)
        
        submitImages(movieDetails?.videos, movieDetails?.backdropPaths)
        
        reviewsAdapter.submitList(movieDetails?.reviews)
        recommendationsAdapter.submitList(movieDetails?.recommendations)
        similarMoviesAdapter.submitList(movieDetails?.similarMovies)
    }
    
    private var imageViewer: StfalconImageViewer<String>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_details_basic)
        
        setSupportActionBar(binding.appBar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.apply {
            lifecycleOwner = this@BasicMovieDetailsActivity
            movieDetails = viewModel.movieDetails
            
            val content = content
            
            content.reviews.reviewsList.adapter = reviewsAdapter
            content.images.imagesList.adapter = imagesAdapter
            content.recommendations.list.adapter = recommendationsAdapter
            content.similarMovies.list.adapter = similarMoviesAdapter
            
            content.images.root.setOnClickListener {
                showMovieDetails()
            }
            
            content.reviews.root.setOnClickListener {
                showAllReviews()
            }
            
            content.recommendations.root.setOnClickListener {
                showDetailsMovies(MoviesType.RECOMMENDATIONS)
            }
            
            content.similarMovies.root.setOnClickListener {
                showDetailsMovies(MoviesType.SIMILAR_MOVIES)
            }
            
            appBar.favouriteStar.source = viewModel
        }
        
        viewModel.movieDetails.observe(this, movieDetailsObserver)
    }
    
    private fun submitImages(videos: List<Video>?, backdrops: List<String>?) {
        val shouldBeGone = videos.isNullOrEmpty()
                && (backdrops == null || backdrops.size <= 1)
                && ! injector.networkInfoProvider.isNetworkAvailable
        
        binding.content.images.imagesList.visibility =
                if(shouldBeGone) GONE
                else VISIBLE
        
        if(! shouldBeGone) {
            imagesAdapter.submitList(videos, backdrops)
        }
    }
    
    private fun updateContentVisibility(movieDetails: MovieDetails?) {
        if(viewModel.isReloading.value == true) return
        
        val content = binding.content
        val emptyView = binding.emptyView
        
        if(movieDetails == null) {
            // Reset the UI
            
            content.root.visibility = GONE
            emptyView.root?.visibility = GONE
            return
        }
        
        if(movieDetails.isMovieFullyCached) {
            content.root.visibility = VISIBLE
            emptyView.root?.visibility = GONE
        } else {
            content.root.visibility = GONE
            
            emptyView.viewStub?.inflate()
            emptyView.root !!.visibility = VISIBLE
        }
    }
    
    private fun showDetailsMovies(type: MoviesType) {
        val listIntent = MoviesActivity.buildIntent(this, viewModel.movieId, type)
        startActivity(listIntent)
    }
    
    private fun showAllReviews(position: Int = ReviewsActivity.NO_POSITION) {
        val reviewsIntent = ReviewsActivity.buildIntent(this, viewModel.movieId, position)
        startActivity(reviewsIntent)
    }
    
    private fun showMovieDetails() {
        val detailsIntent = FullMovieDetailsActivity.buildIntent(this, viewModel.movieId)
        startActivity(detailsIntent)
    }
    
    private fun openTrailer(video: Video) {
        val youTubeUri = YouTubeUriBuilder.buildVideoUri(video.imagePath)
        val youTubeIntent = Intent(Intent.ACTION_VIEW, youTubeUri)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        if(youTubeIntent.resolveActivity(packageManager) != null) {
            startActivity(youTubeIntent)
        }
    }
    
    private fun showImageFullscreen(position: Int, clickedImageView: ImageView? = null) {
        val allImages = viewModel.movieDetails.value?.backdropPaths
        val animage = clickedImageView != null
        
        imageViewer = StfalconImageViewer.Builder<String>(this, allImages) { imageView, imagePath ->
            val imageUri = TmdbUriBuilder.buildBackdropImageUri(applicationContext, imagePath)
            
            GlideApp.with(this)
                    .load(imageUri)
                    .centerInside()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .override(Target.SIZE_ORIGINAL)
                    .into(imageView)
        }
                .withStartPosition(position)
                .allowSwipeToDismiss(true)
                .allowZooming(true)
                .withTransitionFrom(clickedImageView)
                .withDismissListener {
                    imageViewer = null
                }
                .show(animage)
    }
    
    private fun loadPoster(movieDetails: MovieDetails?) {
        binding.header.thumbnail.let { thumbnailView ->
            if(movieDetails != null) {
                getPosterGlideRequest(movieDetails.posterPath)
                        .into(thumbnailView)
            } else {
                GlideUtils.clear(thumbnailView)
            }
        }
    }
    
    private fun loadBackdrop(movieDetails: MovieDetails?) {
        binding.appBar.backdrop.also { backdropView ->
            if(movieDetails != null) {
                val errorRequest = getPosterGlideRequest(movieDetails.posterPath)
                
                val backdropPath = movieDetails.backdropPaths.getOrNull(0)
                
                if(backdropPath != null) {
                    val backdropImageUri = TmdbUriBuilder.buildBackdropImageUri(
                            applicationContext, backdropPath)
                    
                    GlideApp.with(this)
                            .load(backdropImageUri)
                            .centerCrop()
                            .thumbnail(0.15F)
                            .error(errorRequest)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .thumbnail(0.2F)
                            .into(backdropView)
                } else {
                    errorRequest.into(backdropView)
                }
            } else {
                GlideUtils.clear(backdropView)
            }
        }
    }
    
    private fun getPosterGlideRequest(posterPath: String?): GlideRequest<Drawable> {
        val imageUri =
                if(posterPath != null)
                    TmdbUriBuilder.buildPosterImageUri(applicationContext, posterPath)
                else null
        
        return GlideApp.with(this)
                .load(imageUri)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.DATA)
    }
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when(item?.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        imageViewer?.let { imageViewer ->
            outState.putInt(KEY_IMAGE_POSITION_SHOWING_FULLSCREEN, imageViewer.currentPosition())
        }
        
        super.onSaveInstanceState(outState)
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        
        if(savedInstanceState.containsKey(KEY_IMAGE_POSITION_SHOWING_FULLSCREEN)) {
            val position = savedInstanceState.getInt(KEY_IMAGE_POSITION_SHOWING_FULLSCREEN)
            showImageFullscreen(position)
        }
    }
}