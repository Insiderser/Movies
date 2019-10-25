/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.ui.details.basic

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.insiderser.android.movies.R
import com.insiderser.android.movies.api.tmdb.TmdbUriBuilder
import com.insiderser.android.movies.api.youtube.YouTubeUriBuilder
import com.insiderser.android.movies.glide.GlideApp
import com.insiderser.android.movies.glide.GlideImagePreloader
import com.insiderser.android.movies.glide.GlideUtils
import com.insiderser.android.movies.model.HasImagePath
import com.insiderser.android.movies.model.Video
import com.insiderser.android.movies.utils.ImageWidthProvider
import com.insiderser.android.movies.utils.OnItemClickCallback
import com.insiderser.android.movies.utils.extentions.inflate
import kotlinx.android.synthetic.main.list_item_images.view.*

class MovieImagesAdapter(activity: FragmentActivity,
        private val onVideoClickCallback: OnItemClickCallback<Video>,
        private val onMovieImageClickCallback: OnImageClickListener) :
        ListAdapter<HasImagePath, MovieImagesAdapter.ImageViewHolder>(
                ImagePathItemCallback()),
        GlideImagePreloader.Source<Uri> {
    
    companion object {
        private const val TYPE_TRAILERS = 1
        private const val TYPE_BACKDROPS = 2
    }
    
    private val glide = GlideApp.with(activity)
    
    private val applicationContext = activity.applicationContext
    
    private var videos: List<Video> = emptyList()
    private var backdrops: List<Backdrop> = emptyList()
    
    private val imageWidth = ImageWidthProvider.getMovieDetailsImagesWidth(activity)
    private val imageHeight = imageWidth / 16 * 9
    
    fun submitList(videos: List<Video>?, backdrops: List<String>?) {
        this.videos = videos ?: emptyList()
        this.backdrops = backdrops?.map { Backdrop(it) } ?: emptyList()
        super.submitList(this.videos + this.backdrops)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = parent.inflate(R.layout.list_item_images) as ConstraintLayout
        
        if(viewType == TYPE_TRAILERS) {
            itemView.inflate(R.layout.list_item_images_play_icon, attachToRoot = true)
        }
        
        val viewHolder = ImageViewHolder(itemView)
        
        itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            
            when(viewType) {
                TYPE_TRAILERS -> onVideoClickCallback(videos[position])
                TYPE_BACKDROPS -> {
                    onMovieImageClickCallback(position - videos.size, viewHolder.thumbnailImageView)
                }
            }
        }
        
        return viewHolder
    }
    
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItemForPosition(position))
        
        if(getItemViewType(position) == TYPE_TRAILERS) {
            holder.thumbnailImageView.contentDescription = videos[position].name
        }
    }
    
    override fun getItemViewType(position: Int): Int =
            if(position < videos.size) TYPE_TRAILERS
            else TYPE_BACKDROPS
    
    override fun getItemForPosition(position: Int): Uri? = when {
        position < videos.size -> {
            val trailer = videos[position]
            YouTubeUriBuilder.buildVideoThumbnailUri(trailer.imagePath)
        }
        position < (videos.size + backdrops.size) -> {
            val backdrop = backdrops[position - videos.size]
            TmdbUriBuilder.buildBackdropImageUri(applicationContext, backdrop.imagePath)
        }
        else -> null
    }
    
    override fun getGlideRequest(item: Uri) = glide.load(item)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    
    override val imageCount: Int get() = itemCount
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        GlideImagePreloader.attach(this, recyclerView, imageWidth, imageHeight)
    }
    
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        GlideImagePreloader.detach(recyclerView)
    }
    
    override fun onViewRecycled(holder: ImageViewHolder) {
        holder.clear()
    }
    
    @Deprecated(level = DeprecationLevel.HIDDEN,
            message = "Use submitTrailers() and submitBackdrops() instead.")
    override fun submitList(list: MutableList<HasImagePath>?) {
        throw IllegalStateException("You mustn't use submitList.")
    }
    
    private data class Backdrop(override val imagePath: String) : HasImagePath
    
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        val thumbnailImageView: ImageView = itemView.thumbnail
        
        fun bind(imageUri: Uri?) {
            if(imageUri != null) {
                getGlideRequest(imageUri)
                        .thumbnail(0.2F)
                        .into(thumbnailImageView)
            } else {
                clear()
            }
        }
        
        fun clear() {
            GlideUtils.clear(thumbnailImageView)
        }
    }
    
    private class ImagePathItemCallback : DiffUtil.ItemCallback<HasImagePath>() {
        
        override fun areItemsTheSame(oldItem: HasImagePath, newItem: HasImagePath): Boolean =
                oldItem.javaClass == newItem.javaClass && oldItem.imagePath == newItem.imagePath
        
        override fun areContentsTheSame(oldItem: HasImagePath, newItem: HasImagePath): Boolean =
                oldItem == newItem
    }
}

typealias OnImageClickListener = (position: Int, imageView: ImageView) -> Unit