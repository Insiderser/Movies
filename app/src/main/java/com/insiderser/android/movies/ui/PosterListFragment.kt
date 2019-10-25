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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.insiderser.android.movies.R
import com.insiderser.android.movies.databinding.ListPostersBinding
import com.insiderser.android.movies.di.injector
import com.insiderser.android.movies.glide.GlideApp
import com.insiderser.android.movies.glide.GlideRequest
import com.insiderser.android.movies.model.Poster
import com.insiderser.android.movies.utils.extentions.inflate
import com.insiderser.android.movies.utils.extentions.lazyUnsynchronized
import kotlinx.android.synthetic.main.list_item_poster.view.*

abstract class PosterListFragment : Fragment() {
    
    protected abstract val source: Source
    
    @get:LayoutRes
    protected abstract val emptyLayoutRes: Int
    
    protected var shouldSwipeRefrLayoutBeEnabled: Boolean = true
        set(value) {
            field = value
            if(isAdded) {
                binding.swipeRefrLayout.isEnabled = value
            }
        }
    
    protected lateinit var binding: ListPostersBinding
        private set
    
    private val listAdapter by lazyUnsynchronized {
        PosterListAdapter(this)
    }
    
    @CallSuper
    override fun onCreateView(inflater: LayoutInflater,
            container: ViewGroup?, savedInstanceState: Bundle?): View? =
            ListPostersBinding.inflate(inflater, container, false).also {
                binding = it
            }.root
    
    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            lifecycleOwner = this@PosterListFragment
            isRefreshing = source.isRefreshing
            
            list.adapter = listAdapter
            
            swipeRefrLayout.isEnabled = shouldSwipeRefrLayoutBeEnabled
            
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
        
        source.posters.observe(this, Observer(this::submitPosters))
    }
    
    private fun reload() {
        source.reload()
        
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
    
    private fun submitPosters(posters: List<Poster>?) {
        binding.apply {
            val layoutManager = list.layoutManager
            if(layoutManager is LinearLayoutManager && list.scrollY != 0) {
//                Handler(Looper.getMainLooper()).postDelayed({
                list.scrollToPosition(0)
//                }, 200)
            }
            
            listAdapter.submitList(posters)
            
            if(emptyLayoutRes != 0) {
                setEmptyViewVisibility(emptyView, shouldBeVisible = posters.isNullOrEmpty())
            }
            
            list.visibility = if(posters.isNullOrEmpty()) GONE else VISIBLE
        }
    }
    
    protected open fun setEmptyViewVisibility(emptyView: ViewStubProxy, shouldBeVisible: Boolean) {
        if(shouldBeVisible) {
            if(! emptyView.isInflated) {
                emptyView.viewStub !!.inflate()
            }
            emptyView.root !!.visibility = VISIBLE
        } else {
            emptyView.root?.visibility = GONE
        }
    }
    
    interface Source {
        
        val posters: LiveData<List<Poster>>
        
        val isRefreshing: LiveData<Boolean> get() = MutableLiveData()
        
        fun reload() {}
    }
    
    private class PosterListAdapter(fragment: Fragment) :
            PosterAdapter(fragment.requireContext(), GlideApp.with(fragment)) {
        
        override fun onCreatePosterViewHolder(parent: ViewGroup): PosterViewHolder {
            val rootView = parent.inflate(R.layout.list_item_poster)
            return PosterListViewHolder(rootView)
        }
        
        override fun getGlideRequest(item: String): GlideRequest<Drawable> {
            return super.getGlideRequest(item)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
        }
        
        private inner class PosterListViewHolder(rootView: View) : PosterViewHolder(rootView) {
            
            override val imageView: ImageView = rootView.image
        }
    }
}