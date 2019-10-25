/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.utils.extentions

inline fun <T> Iterable<T>.filterLimiting(limit: Int, predicate: (T) -> Boolean): List<T> = when {
    limit == 0 -> emptyList()
    limit > 0 -> mutableListOf<T>().let { destination ->
        this.filterTo(destination) {
            destination.size < limit && predicate(it)
        }
    }
    else -> throw IllegalArgumentException("Invalid limit $limit; must be >= 0")
}

inline fun <T, R : Any> Iterable<T>.mapLimiting(limit: Int, predicate: (T) -> R): List<R> = when {
    limit == 0 -> emptyList()
    limit > 0 -> mutableListOf<R>().let { destination ->
        this.mapNotNullTo(destination) {
            if(destination.size >= limit)
                null
            else
                predicate(it)
        }
    }
    else -> throw IllegalArgumentException("Invalid limit $limit; must be >= 0")
}

fun <T> MutableList<T>.removeLast() {
    this.removeAt(this.lastIndex)
}