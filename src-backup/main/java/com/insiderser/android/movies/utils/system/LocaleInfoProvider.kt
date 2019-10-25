/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.utils.system

import android.app.Application
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.N
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleInfoProvider @Inject constructor(private val application: Application) {
    
    val language: String
        get() = userLocale.language
    
    val region: String
        get() = userLocale.country
    
    private val userLocale: Locale
        get() = if(SDK_INT >= N) {
            application.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            application.resources.configuration.locale
        }
}