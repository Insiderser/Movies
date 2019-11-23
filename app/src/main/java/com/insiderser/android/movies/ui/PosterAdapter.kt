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

package com.insiderser.android.movies.ui

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.insiderser.android.movies.MoviesApp
import com.insiderser.android.movies.api.tmdb.TmdbUriBuilder
import com.insiderser.android.movies.glide.GlideImagePreloader
import com.insiderser.android.movies.glide.GlideRequest
import com.insiderser.android.movies.glide.GlideRequests
import com.insiderser.android.movies.glide.GlideUtils
import com.insiderser.android.movies.model.Poster
import com.insiderser.android.movies.model.movie.Movie
import com.insiderser.android.movies.model.tv.TvShow
import com.insiderser.android.movies.model.types.Type
import com.insiderser.android.movies.ui.details.basic.BasicDetailsActivity
import com.insiderser.android.movies.utils.extentions.inflate

abstract class PosterAdapter(
    private val activity: Activity,
    private val glideRequests: GlideRequests,
    private val displayShowMoreItem: Boolean = false,
    private val onShowMoreClickCallback: OnShowMoreClickCallback? = null,
    @LayoutRes private val showMoreItemLayoutRes: Int = 0) :
    ListAdapter<Poster, RecyclerView.ViewHolder>(PosterDiffItemCallback()),
    GlideImagePreloader.Source<String> {

    val context: Context = activity

    private val itemImageWidth: Int

    private val uriBuilder: TmdbUriBuilder

    init {
        val application = context.applicationContext as MoviesApp
        val injector = application.injector

        itemImageWidth = injector.imageWidthProvider.getPosterWidth()
        uriBuilder = injector.tmdbUriBuilder
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_POSTER -> onCreatePosterViewHolder(parent)
            VIEW_TYPE_SHOW_MORE -> onCreateShowMoreViewHolder(parent)
            else -> throw IllegalStateException("Invalid view type: $viewType")
        }

    abstract fun onCreatePosterViewHolder(parent: ViewGroup): PosterViewHolder

    open fun onCreateShowMoreViewHolder(parent: ViewGroup): ShowMoreViewHolder {
        val layoutRes = showMoreItemLayoutRes

        check(layoutRes != 0) {
            "You must initialize showMoreItemLayoutRes " +
                "before a show-more item is created"
        }

        val itemView = parent.inflate(layoutRes)

        return ShowMoreViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PosterViewHolder) {
            holder.bind(getItem(position))
        }
    }

    override fun getItemForPosition(position: Int): String? {
        if (position >= imageCount) return null

        return getItem(position).posterPath
    }

    override fun getGlideRequest(item: String): GlideRequest<Drawable> {
        val imageUri = uriBuilder.buildPosterImageUri(item)

        return glideRequests.load(imageUri)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    }

    override val imageCount: Int get() = super.getItemCount()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        GlideImagePreloader.attach(this, recyclerView, itemImageWidth,
            imageHeight = itemImageWidth * 3 / 2)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        GlideImagePreloader.detach(recyclerView)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is PosterViewHolder) {
            holder.clear()
        }
    }

    override fun getItemCount(): Int {
        val itemCount = super.getItemCount()

        return if (displayShowMoreItem) {
            if (itemCount != 0)
                itemCount + 1
            else 0
        } else itemCount
    }

    override fun getItemViewType(position: Int): Int =
        if (displayShowMoreItem && position == super.getItemCount())
            VIEW_TYPE_SHOW_MORE
        else VIEW_TYPE_POSTER

    abstract inner class PosterViewHolder(rootView: View) :
        RecyclerView.ViewHolder(rootView) {

        protected abstract val imageView: ImageView

        init {
            rootView.setOnClickListener {
                val detailsActivityIntent = when (val poster = getItem(adapterPosition)) {
                    is Movie -> BasicDetailsActivity.buildIntent(context, poster.id, Type.MOVIE)
                    is TvShow -> BasicDetailsActivity.buildIntent(context, poster.id, Type.TV_SHOW)
                    else -> throw IllegalStateException("Unknown Poster implementation: $poster")
                }

                val transitionName = imageView.transitionName
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity, imageView, transitionName)

                context.startActivity(detailsActivityIntent, options.toBundle())
            }
        }

        fun bind(item: Poster) {
            imageView.contentDescription = item.title

            val posterPath = item.posterPath
            if (posterPath != null) {
                getGlideRequest(posterPath)
                    .thumbnail(0.25F)
                    .into(imageView)
            } else {
                clear()
            }
        }

        fun clear() {
            GlideUtils.clear(imageView)
        }
    }

    inner class ShowMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                onShowMoreClickCallback?.invoke()
            }
        }
    }

    private class PosterDiffItemCallback : DiffUtil.ItemCallback<Poster>() {

        override fun areItemsTheSame(oldItem: Poster, newItem: Poster): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Poster, newItem: Poster): Boolean =
            oldItem == newItem
    }

    companion object {
        private const val VIEW_TYPE_POSTER = 1
        private const val VIEW_TYPE_SHOW_MORE = 2
    }
}

typealias OnShowMoreClickCallback = () -> Unit
