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
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout
import com.insiderser.android.movies.R
import com.insiderser.android.movies.api.youtube.YouTubeUriBuilder
import com.insiderser.android.movies.databinding.ActivityDetailsBasicBinding
import com.insiderser.android.movies.di.injector
import com.insiderser.android.movies.glide.GlideApp
import com.insiderser.android.movies.glide.GlideRequest
import com.insiderser.android.movies.glide.GlideUtils
import com.insiderser.android.movies.model.PosterDetails
import com.insiderser.android.movies.model.Video
import com.insiderser.android.movies.model.types.MoviesSuggestionsType
import com.insiderser.android.movies.model.types.Type
import com.insiderser.android.movies.model.types.toInt
import com.insiderser.android.movies.model.types.toType
import com.insiderser.android.movies.ui.ReviewsAdapter
import com.insiderser.android.movies.ui.details.full.FullMovieDetailsActivity
import com.insiderser.android.movies.ui.recommendations.RecommendationsActivity
import com.insiderser.android.movies.ui.reviews.ReviewsActivity
import com.insiderser.android.movies.utils.extentions.afterLayout
import com.insiderser.android.movies.utils.extentions.lazyUnsynchronized
import com.insiderser.android.movies.utils.extentions.roundToHigherInt
import com.insiderser.android.movies.utils.extentions.statusBarHeight
import com.insiderser.android.movies.utils.extentions.viewModelProvider
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlin.math.abs

class BasicDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_TYPE = "type"
        const val EXTRA_TRANSITION_NAME = "transition_name"

        @JvmStatic
        fun buildIntent(context: Context,
                id: Int,
                type: Type,
                transitionName: String? = null): Intent =
                Intent(context, BasicDetailsActivity::class.java)
                        .putExtra(EXTRA_ID, id)
                        .putExtra(EXTRA_TYPE, type.toInt())
                        .putExtra(EXTRA_TRANSITION_NAME, transitionName)

        private const val KEY_IMAGE_POSITION_SHOWING_FULLSCREEN = "image position showing fullscreen"
    }

    private val viewModel: BasicDetailsViewModel by viewModelProvider {
        val intent = intent

        check(intent.hasExtra(EXTRA_ID)) { "ID must be passed as intent extra" }
        check(intent.hasExtra(EXTRA_TYPE)) { "Type must be passed as intent extra" }

        val movieId = intent.getIntExtra(EXTRA_ID, -1)
        val type = intent.getIntExtra(EXTRA_TYPE, -1).toType()
        injector.getBasicMovieDetailsViewModel().also { viewModel ->
            viewModel.initState(movieId, type)
        }
    }

    private lateinit var binding: ActivityDetailsBasicBinding

    private val imagesAdapter by lazyUnsynchronized {
        MovieImagesAdapter(this,
                onVideoClickCallback = this::openTrailer,
                onMovieImageClickCallback = this::showImageFullscreen)
    }

    private val recommendationsAdapter by lazyUnsynchronized {
        RecommendationsAdapter(this, onShowMoreClickCallback = {
            showDetailsMovies(
                    MoviesSuggestionsType.RECOMMENDATIONS)
        })
    }

    private val similarMoviesAdapter by lazyUnsynchronized {
        RecommendationsAdapter(this, onShowMoreClickCallback = {
            showDetailsMovies(
                    MoviesSuggestionsType.SIMILAR_MOVIES)
        })
    }

    private val reviewsAdapter by lazyUnsynchronized {
        ReviewsAdapter(maxReviewsCount = 2,
                onItemClickCallback = { position ->
                    showAllReviews(position)
                })
    }

    private var imageViewer: StfalconImageViewer<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_details_basic)

        supportPostponeEnterTransition()

        binding.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            if (intent.hasExtra(EXTRA_TRANSITION_NAME)) {
                header.thumbnailCardView.transitionName = intent.getStringExtra(EXTRA_TRANSITION_NAME)
            }

            lifecycleOwner = this@BasicDetailsActivity
            details = viewModel.details

            reviews.reviewsList.adapter = reviewsAdapter
            images.imagesList.adapter = imagesAdapter
            recommendations.list.adapter = recommendationsAdapter
            similarMovies.list.adapter = similarMoviesAdapter

            images.root.setOnClickListener {
                showMovieDetails()
            }

            reviews.root.setOnClickListener {
                showAllReviews()
            }

            recommendations.root.setOnClickListener {
                showDetailsMovies(MoviesSuggestionsType.RECOMMENDATIONS)
            }

            similarMovies.root.setOnClickListener {
                showDetailsMovies(MoviesSuggestionsType.SIMILAR_MOVIES)
            }

            favouriteStar.source = viewModel

            appBar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
                private var previousOffset: Int = 0

                override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                    val normalizedOffset = abs(verticalOffset)
                    val previousOffset = previousOffset

                    if (normalizedOffset > previousOffset) {
                        scrollView.scrollTo(0, normalizedOffset)
                    }

                    this.previousOffset = normalizedOffset
                }
            })

            /*scrollView.setOnScrollChangeListener { _: NestedScrollView, _, scrollY, _, oldScrollY ->
                val totalScrollRange = appBar.totalScrollRange

                val offsetBy = if(scrollY < oldScrollY && scrollY < totalScrollRange) {
                    min(totalScrollRange, oldScrollY) - scrollY
                } else if(scrollY > oldScrollY && oldScrollY < totalScrollRange) {
                    oldScrollY - max(totalScrollRange, scrollY)
                } else
                    return@setOnScrollChangeListener

                ViewCompat.offsetTopAndBottom(appBar, offsetBy)
            }*/

            header.root.afterLayout {
                val paddingTop = (backdrop.height - statusBarHeight -
                        toolbar.height - header.thumbnailCardView.height / 2F)
                        .roundToHigherInt()

                this.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)

                false
            }

        }

        viewModel.details.observe(this, Observer { details: PosterDetails? ->
            updateContentVisibility(details)

            loadPoster(details)
            loadBackdrop(details)

            submitImages(details?.videos, details?.backdropPaths)

            reviewsAdapter.submitList(details?.reviews)
            recommendationsAdapter.submitList(details?.recommendations)
            similarMoviesAdapter.submitList(details?.similar)
        })
    }

    private fun submitImages(videos: List<Video>?, backdrops: List<String>?) {
        val isMovieFullyCached = (!videos.isNullOrEmpty())
                || (backdrops != null && backdrops.size > 1)

        binding.images.root.visibility =
                if (!isMovieFullyCached) GONE
                else VISIBLE

        if (isMovieFullyCached) {
            imagesAdapter.submitList(videos, backdrops)
        }
    }

    private fun updateContentVisibility(details: PosterDetails?) {
        val binding = binding
        val emptyView = binding.emptyView

        val contentVisibility = when {
            details == null -> {
                emptyView.root?.visibility = GONE
                GONE
            }
            details.isFullyCached -> {
                emptyView.root?.visibility = GONE
                VISIBLE
            }
            viewModel.isReloading.value == true -> GONE
            else -> {
                emptyView.viewStub?.inflate()
                emptyView.root!!.visibility = VISIBLE
                GONE
            }
        }

        binding.images.root.visibility = contentVisibility
        binding.reviews.root.visibility = contentVisibility
        binding.recommendations.root.visibility = contentVisibility
        binding.similarMovies.root.visibility = contentVisibility
    }

    private fun showDetailsMovies(type: MoviesSuggestionsType) {
        val listIntent = RecommendationsActivity.buildIntent(this, viewModel.id, type)
        startActivity(listIntent)
    }

    private fun showAllReviews(position: Int = ReviewsActivity.NO_POSITION) {
        val reviewsIntent = ReviewsActivity.buildIntent(this, viewModel.id, position)
        startActivity(reviewsIntent)
    }

    private fun showMovieDetails() {
        val detailsIntent = FullMovieDetailsActivity.buildIntent(this, viewModel.id)
        startActivity(detailsIntent)
    }

    private fun openTrailer(video: Video) {
        val youTubeUri = YouTubeUriBuilder.buildVideoUri(video.imagePath)
        val youTubeIntent = Intent(Intent.ACTION_VIEW, youTubeUri)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (youTubeIntent.resolveActivity(packageManager) != null) {
            startActivity(youTubeIntent)
        }
    }

    private fun showImageFullscreen(position: Int, clickedImageView: ImageView? = null) {
        val allImages = viewModel.details.value?.backdropPaths
        val animate = clickedImageView != null

        imageViewer = StfalconImageViewer.Builder(this, allImages) { imageView, imagePath ->
            val imageUri = injector.tmdbUriBuilder.buildBackdropImageUri(imagePath)

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
                .show(animate)
    }

    private fun loadPoster(details: PosterDetails?) {
        binding.header.thumbnail.let { thumbnailView ->
            if (details != null) {
                getPosterGlideRequest(details.posterPath)
                        .listener(ResumePostponedTransitionRequestListener())
                        .into(thumbnailView)
            } else {
                GlideUtils.clear(thumbnailView)
            }
        }
    }

    private fun loadBackdrop(details: PosterDetails?) {
        binding.backdrop.also { backdropView ->
            if (details != null) {
                val errorRequest = getPosterGlideRequest(details.posterPath)

                val backdropPath = details.backdropPaths.getOrNull(0)

                if (backdropPath != null) {
                    val backdropImageUri = injector.tmdbUriBuilder.buildBackdropImageUri(
                            backdropPath)

                    GlideApp.with(this)
                            .load(backdropImageUri)
                            .centerCrop()
                            .thumbnail(0.15F)
                            .priority(Priority.HIGH)
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
                if (posterPath != null)
                    injector.tmdbUriBuilder.buildPosterImageUri(posterPath)
                else null

        return GlideApp.with(this)
                .load(imageUri)
                .centerCrop()
                .priority(Priority.IMMEDIATE)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            supportFinishAfterTransition()
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

        if (savedInstanceState.containsKey(KEY_IMAGE_POSITION_SHOWING_FULLSCREEN)) {
            val position = savedInstanceState.getInt(KEY_IMAGE_POSITION_SHOWING_FULLSCREEN)
            showImageFullscreen(position)
        }
    }

    inner class ResumePostponedTransitionRequestListener : RequestListener<Drawable?> {
        override fun onLoadFailed(e: GlideException?,
                model: Any?,
                target: Target<Drawable?>?,
                isFirstResource: Boolean): Boolean {
            supportStartPostponedEnterTransition()
            return false
        }

        override fun onResourceReady(resource: Drawable?,
                model: Any?,
                target: Target<Drawable?>?,
                dataSource: DataSource?,
                isFirstResource: Boolean): Boolean {
            supportStartPostponedEnterTransition()
            return false
        }
    }
}
