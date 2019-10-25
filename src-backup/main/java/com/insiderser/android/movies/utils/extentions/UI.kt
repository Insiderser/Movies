/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.utils.extentions

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.insiderser.android.movies.R

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
    beginTransaction().apply(func).commit()
}

fun ViewGroup.inflate(@LayoutRes resource: Int, attachToRoot: Boolean = false): View =
        LayoutInflater.from(context).inflate(resource, this, attachToRoot)

val FragmentActivity.hasTaskParents: Boolean get() = ! this.isTaskRoot

fun Uri.openInBrowser(context: Context) {
    val toolbarColor = ResourcesCompat.getColor(context.resources, R.color.colorPrimary,
            context.theme)
    
    CustomTabsIntent.Builder()
            .setToolbarColor(toolbarColor)
            .setShowTitle(true)
            .addDefaultShareMenuItem()
            .build()
            .launchUrl(context, this)
}