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

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.insiderser.android.movies.R
import com.insiderser.android.movies.glide.GlideApp
import com.insiderser.android.movies.ui.OnShowMoreClickCallback
import com.insiderser.android.movies.ui.PosterAdapter
import com.insiderser.android.movies.utils.extentions.inflate
import kotlinx.android.synthetic.main.list_item_recommendations.view.*

class RecommendationsAdapter(
        activity: FragmentActivity,
        onShowMoreClickCallback: OnShowMoreClickCallback) :
        PosterAdapter(activity, GlideApp.with(activity), true, onShowMoreClickCallback,
                R.layout.list_item_recommendations_show_more) {
    
    override fun onCreatePosterViewHolder(parent: ViewGroup): PosterViewHolder {
        val rootView = parent.inflate(R.layout.list_item_recommendations)
        return RecommendationsPosterViewHolder(rootView)
    }
    
    private inner class RecommendationsPosterViewHolder(rootView: View) :
            PosterViewHolder(rootView) {
        
        override val imageView: ImageView = rootView.image
    }
}