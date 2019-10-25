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

package com.insiderser.android.movies.databinding

import androidx.databinding.BindingAdapter
import com.insiderser.android.movies.R
import com.insiderser.android.movies.databinding.DetailsBindingAdapters.setVisibleOrGone
import com.insiderser.android.movies.model.ReleaseDate
import com.insiderser.android.movies.utils.StringFormatter
import com.insiderser.android.movies.widget.DetailView

object DetailsFullBindingAdapters {
    
    @JvmStatic
    @BindingAdapter("runtime")
    fun setRuntime(detailView: DetailView, runtime: Int) {
        check(runtime >= 0) { "Invalid runtime: $runtime" }
        
        val context = detailView.context
        
        val hours = runtime / 60
        val minutes = runtime % 60
        
        val h = context.getString(R.string.hours_short)
        val m = context.getString(R.string.minutes_short)
        
        detailView.value = formatRuntime(hours, minutes, h, m)
    }
    
    @JvmStatic
    private fun formatRuntime(hours: Int, minutes: Int, h: String, m: String): String {
        check(hours >= 0) { "Invalid hours: $hours" }
        check(minutes >= 0) { "Invalid minutes: $minutes" }
        
        if(hours == 0 && minutes == 0) {
            return "-$h -$m"
        }
        
        return buildString {
            if(hours != 0) {
                append(hours).append(h)
            }
            
            if(hours != 0 && minutes != 0) {
                append(" ")
            }
            
            if(minutes != 0) {
                append(minutes)
                append(m)
            }
        }
    }
    
    @JvmStatic
    @BindingAdapter("certificate")
    fun setCertificate(detailView: DetailView, releaseDates: List<ReleaseDate>?) {
        val certificates = StringFormatter.formatCertification(releaseDates)
        
        detailView.value = certificates
        
        setVisibleOrGone(detailView, certificates)
    }
}