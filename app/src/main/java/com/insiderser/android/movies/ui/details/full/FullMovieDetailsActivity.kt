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

package com.insiderser.android.movies.ui.details.full

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.insiderser.android.movies.R
import com.insiderser.android.movies.api.imdb.ImdbUriBuilder
import com.insiderser.android.movies.databinding.ActivityDetailsFullBinding
import com.insiderser.android.movies.di.injector
import com.insiderser.android.movies.utils.extentions.viewModelProvider

class FullMovieDetailsActivity : AppCompatActivity() {

    companion object {

        const val EXTRA_MOVIE_ID = "movie_id"

        @JvmStatic
        fun buildIntent(context: Context, movieId: Int): Intent =
                Intent(context, FullMovieDetailsActivity::class.java)
                        .putExtra(EXTRA_MOVIE_ID, movieId)
    }

    private lateinit var binding: ActivityDetailsFullBinding

    private val viewModel: FullMovieDetailsViewModel by viewModelProvider {
        injector.getFullMovieDetailsViewModel().also { viewModel ->
            val intent = intent

            check(intent.hasExtra(EXTRA_MOVIE_ID)) { "Movie ID must be passed as intent extra" }

            val movieId = intent.getIntExtra(EXTRA_MOVIE_ID, 0)

            viewModel.initState(movieId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_details_full)

        binding.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            lifecycleOwner = this@FullMovieDetailsActivity
            movieDetails = viewModel.movieDetails

            openTmdb.setOnClickListener {
                openMovieTmdb()
            }

            openImdb.setOnClickListener {
                openMovieImdb()
            }
        }
    }

    private fun openMovieTmdb() {
        val tmdbMovieUri = injector.tmdbUriBuilder.buildMovieUri(viewModel.movieId)
        openInBrowserCustomTabs(tmdbMovieUri)
    }

    private fun openMovieImdb() {
        val tmdbMovieUri = ImdbUriBuilder.buildMovieUri(viewModel.movieDetails.value!!.imdbId!!)
        openInBrowserCustomTabs(tmdbMovieUri)
    }

    private fun openInBrowserCustomTabs(uri: Uri) {
        val toolbarColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, theme)

        CustomTabsIntent.Builder()
                .setToolbarColor(toolbarColor)
                .setShowTitle(true)
                .addDefaultShareMenuItem()
                .build()
                .launchUrl(this, uri)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
