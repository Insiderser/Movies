/*
 * Copyright 2019 Oleksandr Bezushko
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.model.search_suggestion

import android.os.Parcel
import android.os.Parcelable
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion

data class HistorySearchSuggestion(val query: String) : SearchSuggestion {

    override fun getBody(): String = query

    constructor(parcel: Parcel) : this(parcel.readString() !!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(query)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<HistorySearchSuggestion> {
        override fun createFromParcel(parcel: Parcel): HistorySearchSuggestion =
                HistorySearchSuggestion(
                        parcel)

        override fun newArray(size: Int): Array<HistorySearchSuggestion?> = arrayOfNulls(size)
    }
}
