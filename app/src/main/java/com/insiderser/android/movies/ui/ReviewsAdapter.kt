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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.insiderser.android.movies.databinding.ListItemReviewsBinding
import com.insiderser.android.movies.model.Review

class ReviewsAdapter(private val maxReviewsCount: Int = -1,
        private val onItemClickCallback: OnReviewItemClickCallback? = null) :
        ListAdapter<Review, ReviewsAdapter.ReviewsViewHolder>(ReviewDiffItemCallback()) {

    private val limitReviewsCount = maxReviewsCount >= 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        val binding = ListItemReviewsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)

        return ReviewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        val itemCount = super.getItemCount()

        return if (!limitReviewsCount || itemCount <= maxReviewsCount) itemCount
        else maxReviewsCount
    }

    override fun onViewRecycled(holder: ReviewsViewHolder) {
        holder.bind(null)
    }

    inner class ReviewsViewHolder(private val binding: ListItemReviewsBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            if (!limitReviewsCount) {
                binding.wrapper.background = null
            }

            if (onItemClickCallback != null) {
                binding.wrapper.setOnClickListener {
                    onItemClickCallback.invoke(adapterPosition)
                }
            }
        }

        fun bind(item: Review?) {
            binding.review = item
        }
    }

    private class ReviewDiffItemCallback : DiffUtil.ItemCallback<Review>() {

        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean =
                oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean =
                oldItem == newItem
    }
}

typealias OnReviewItemClickCallback = (position: Int) -> Unit
