/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.utils.extentions

import com.insiderser.android.movies.model.NO_LIMIT
import kotlin.math.min

inline fun <T> Iterable<T>.filter(limit: Int, predicate: (T) -> Boolean): List<T> = when {
    limit == NO_LIMIT -> this.filter(predicate)
    limit < 0 -> throw IllegalArgumentException("Invalid limit $limit; must be >= 0")
    limit == 0 -> emptyList()
    else -> try {
        mutableListOf<T>().also { destination ->
            for(element in this) {
                if(predicate(element)) {
                    destination += element
                    
                    if(destination.size >= limit) break
                }
            }
        }
    } catch(e: ConcurrentModificationException) {
        throw ConcurrentModificationException("You cannot modify Collection while filtering")
    }
}

inline fun <T, R> Collection<T>.map(limit: Int, transform: (T) -> R): List<R> = when {
    limit == NO_LIMIT -> this.map(transform)
    limit < 0 -> throw IllegalArgumentException("Invalid limit $limit; must be >= 0")
    limit == 0 || isEmpty() -> emptyList()
    limit == 1 || size == 1 -> listOf(transform(first()))
    else -> try {
        val iterator = this.iterator()
        val desiredSize = min(size, limit)
        
        List(desiredSize) {
            val item = iterator.next()
            
            transform(item)
        }
    } catch(e: ConcurrentModificationException) {
        throw ConcurrentModificationException("You cannot modify Collection while mapping")
    }
}