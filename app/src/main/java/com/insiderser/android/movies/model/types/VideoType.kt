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

package com.insiderser.android.movies.model.types

import com.insiderser.android.movies.BuildConfig

enum class VideoType {
    TRAILER, TEASER, CLIP, FEATURETTE, BEHIND_THE_SCENES, BLOOPERS
}

fun String.toType() = when(this) {
    "Trailer" -> VideoType.TRAILER
    "Teaser" -> VideoType.TEASER
    "Clip" -> VideoType.CLIP
    "Featurette" -> VideoType.FEATURETTE
    "Behind the Scenes" -> VideoType.BEHIND_THE_SCENES
    "Bloopers" -> VideoType.BLOOPERS
    else ->
        if(BuildConfig.DEBUG)
            throw IllegalArgumentException("Unknown VideoType: $this")
        else null
}