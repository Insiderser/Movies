/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.ui.main

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.insiderser.android.movies.R
import com.insiderser.android.movies.di.injector
import com.insiderser.android.movies.model.sort_by.MovieSortBy
import com.insiderser.android.movies.utils.extentions.viewModelProvider

class SortByDialogFragment : DialogFragment() {
    
    private val viewModel: SortByDialogViewModel by viewModelProvider {
        injector.getSortByViewModel()
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val displayEntries = arrayOf(requireContext().getText(R.string.popularity),
                requireContext().getText(R.string.vote_count),
                requireContext().getText(R.string.revenue))
        
        check(displayEntries.size == MovieSortBy.values().size) {
            "Looks like you have changed MovieSortBy entries " +
                    "but forgot to update SortByDialogFragment's displayEntries"
        }
        
        return AlertDialog.Builder(requireContext())
                .setSingleChoiceItems(displayEntries, viewModel.sortByPosition) { _, position ->
                    viewModel.sortByPosition = position
                    dismiss()
                }
                .create()
    }
    
    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }
    
    companion object {
        
        @JvmField
        val TAG: String = SortByDialogFragment::class.java.name
    }
}