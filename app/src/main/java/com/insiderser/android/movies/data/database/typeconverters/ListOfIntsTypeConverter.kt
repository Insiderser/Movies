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

package com.insiderser.android.movies.data.database.typeconverters

import androidx.room.TypeConverter

object ListOfIntsTypeConverter {
    
    private const val SEPARATOR = ","
    private const val EMPTY = ""
    
    @JvmStatic
    @TypeConverter
    fun fromList(genreIds: List<Int>?): String? =
            when {
                genreIds == null -> null
                genreIds.isEmpty() -> EMPTY
                else -> genreIds.joinToString(SEPARATOR) { it.toString() }
            }
    
    @JvmStatic
    @TypeConverter
    fun toList(string: String?): List<Int>? =
            when(string) {
                null -> null
                EMPTY -> emptyList()
                else -> string.split(SEPARATOR).map { it.toInt() }
            }
}