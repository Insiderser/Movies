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

package com.insiderser.android.movies.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.insiderser.android.movies.R

class StarView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null) :
        AppCompatCheckBox(context, attrs, android.R.attr.starStyle) {
    
    private val starCheckedObserver = Observer<Boolean> { isChecked ->
        setChecked(isChecked)
    }
    
    var source: Source? = null
        set(value) {
            field?.isStarChecked?.removeObserver(starCheckedObserver)
            
            field = value
            
            source?.isStarChecked?.observe(context as LifecycleOwner, starCheckedObserver)
        }
    
    init {
        super.setOnCheckedChangeListener { _, isChecked ->
            source?.apply {
                if(isChecked) onChecked()
                else onUnchecked()
            }
            
            contentDescription = context.getString(
                    if(isChecked) R.string.unfavourite
                    else R.string.favourite)
        }
    }
    
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "For internal use only")
    override fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        throw IllegalStateException("For internal use only")
    }
    
    interface Source {
        
        val isStarChecked: LiveData<Boolean>
        
        fun onChecked()
        
        fun onUnchecked()
    }
}