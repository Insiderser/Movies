/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.model.types

enum class ImageType { BACKDROP, LOGO, POSTER, PROFILE, STILL }

private const val BACKDROP = 0
private const val LOGO = 1
private const val POSTER = 2
private const val PROFILE = 3
private const val STILL = 4

fun ImageType.toInt() = when(this) {
    ImageType.BACKDROP -> BACKDROP
    ImageType.LOGO -> LOGO
    ImageType.POSTER -> POSTER
    ImageType.PROFILE -> PROFILE
    ImageType.STILL -> STILL
}

fun Int.toImageType() = when(this) {
    BACKDROP -> ImageType.BACKDROP
    LOGO -> ImageType.LOGO
    POSTER -> ImageType.POSTER
    PROFILE -> ImageType.PROFILE
    STILL -> ImageType.STILL
    else -> throw IllegalArgumentException("Cannot convert $this to ImageType")
}