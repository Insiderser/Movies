/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.databinding

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter

object DetailsBindingAdapters {
    
    @JvmStatic
    @BindingAdapter("visibleOrGone")
    fun setVisibleOrGone(view: View, value: Any?) {
        view.visibility = when(value) {
            null -> GONE
            is CharSequence -> if(value.isEmpty()) GONE else VISIBLE
            is Collection<*> -> if(value.isEmpty()) GONE else VISIBLE
            is Number -> if(value.toInt() == 0) GONE else VISIBLE
            else -> VISIBLE
        }
    }
    
    @JvmStatic
    @BindingAdapter("visibleOrGone")
    fun setVisibleOrGone(view: View, value: Int) {
        view.visibility = if(value == 0) GONE else VISIBLE
    }
    
    @JvmStatic
    @BindingAdapter("visibleOrGone")
    fun setVisibleOrGone(view: View, value: Long) {
        view.visibility = if(value == 0L) GONE else VISIBLE
    }
    
    @JvmStatic
    @BindingAdapter("horizontalWeight")
    fun setHorizontalWeight(view: View, weight: Float) {
        (view.layoutParams as? ConstraintLayout.LayoutParams)?.let { params ->
            params.horizontalWeight = weight
            view.layoutParams = params
        }
    }
}