/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.databinding.ViewStubProxy
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.insiderser.android.movies.R
import com.insiderser.android.movies.databinding.ListMoviesBinding
import com.insiderser.android.movies.di.injector
import com.insiderser.android.movies.glide.GlideApp
import com.insiderser.android.movies.glide.GlideRequest
import com.insiderser.android.movies.model.Movie
import com.insiderser.android.movies.utils.extentions.inflate
import com.insiderser.android.movies.utils.extentions.lazyUnsynchronized
import kotlinx.android.synthetic.main.list_item_poster.view.*

abstract class MoviesListFragment : Fragment() {
    
    protected abstract val source: Source
    
    @get:LayoutRes
    protected abstract val emptyLayoutRes: Int
    
    private lateinit var binding: ListMoviesBinding
    
    private val moviesAdapter by lazyUnsynchronized {
        PosterListMovieAdapter(this)
    }
    
    @CallSuper
    override fun onCreateView(inflater: LayoutInflater,
            container: ViewGroup?, savedInstanceState: Bundle?): View? =
            ListMoviesBinding.inflate(inflater, container, false).also {
                binding = it
            }.root
    
    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            lifecycleOwner = this@MoviesListFragment
            isRefreshing = source.isRefreshing
            
            movieList.adapter = moviesAdapter
            
            swipeRefrLayout.setOnRefreshListener {
                reload()
            }
            
            swipeRefrLayout.setColorSchemeResources(android.R.color.holo_red_dark,
                    android.R.color.holo_green_dark,
                    android.R.color.holo_blue_dark,
                    android.R.color.holo_orange_dark)
            
            if(emptyLayoutRes != 0) {
                emptyView.viewStub?.layoutResource = emptyLayoutRes
                emptyView.setOnInflateListener { _, inflated ->
                    inflated.setOnClickListener {
                        reload()
                    }
                }
            }
        }
        
        source.movies.observe(this, Observer(this::submitMovies))
    }
    
    private fun reload() {
        source.reloadMovies()
        
        val isNetworkAvailable = injector.networkInfoProvider.isNetworkAvailable
        
        if(! isNetworkAvailable) {
            Snackbar.make(binding.root, R.string.no_internet, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) {
                        reload()
                    }
                    .show()
            
            binding.swipeRefrLayout.isRefreshing = false
        }
    }
    
    private fun submitMovies(movies: List<Movie>?) {
        moviesAdapter.submitList(movies)
        
        if(emptyLayoutRes != 0) {
            setEmptyViewVisibility(binding.emptyView, shouldBeVisible = movies.isNullOrEmpty())
        }
        
        if(movies.isNullOrEmpty()) {
            binding.movieList.visibility = GONE
        } else {
            binding.movieList.visibility = VISIBLE
        }
    }
    
    protected open fun setEmptyViewVisibility(emptyView: ViewStubProxy, shouldBeVisible: Boolean) {
        if(shouldBeVisible) {
            if(! emptyView.isInflated) {
                emptyView.viewStub !!.inflate()
            } else {
                emptyView.root !!.visibility = VISIBLE
            }
        } else {
            binding.emptyView.root?.visibility = GONE
        }
    }
    
    interface Source {
        
        val movies: LiveData<List<Movie>>
        
        val isRefreshing: LiveData<Boolean>
        
        fun reloadMovies()
    }
    
    private class PosterListMovieAdapter(fragment: Fragment) :
            MovieAdapter(fragment.requireContext(), GlideApp.with(fragment)) {
        
        override fun onCreateMovieViewHolder(parent: ViewGroup): MovieViewHolder {
            val rootView = parent.inflate(R.layout.list_item_poster)
            return PosterMovieViewHolder(rootView)
        }
        
        override fun getGlideRequest(item: String): GlideRequest<Drawable> {
            return super.getGlideRequest(item)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
        }
        
        private inner class PosterMovieViewHolder(rootView: View) : MovieViewHolder(rootView) {
            
            override val imageView: ImageView = rootView.image
        }
    }
}