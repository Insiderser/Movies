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

package com.insiderser.android.movies.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.FloatingSearchView.LEFT_ACTION_MODE_SHOW_HAMBURGER
import com.arlib.floatingsearchview.FloatingSearchView.LEFT_ACTION_MODE_SHOW_HOME
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.AppBarLayout
import com.insiderser.android.movies.R
import com.insiderser.android.movies.di.injector
import com.insiderser.android.movies.glide.GlideApp
import com.insiderser.android.movies.glide.GlideUtils
import com.insiderser.android.movies.model.search_suggestion.HistorySearchSuggestion
import com.insiderser.android.movies.model.search_suggestion.PosterSearchSuggestion
import com.insiderser.android.movies.model.search_suggestion.type
import com.insiderser.android.movies.model.types.Type
import com.insiderser.android.movies.ui.about.AboutActivity
import com.insiderser.android.movies.ui.details.basic.BasicDetailsActivity
import com.insiderser.android.movies.utils.extentions.inTransaction
import com.insiderser.android.movies.utils.extentions.viewModelProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
        FragmentManager.OnBackStackChangedListener,
        FloatingSearchView.OnSearchListener,
        FloatingSearchView.OnFocusChangeListener,
        FloatingSearchView.OnLeftMenuClickListener,
        SearchSuggestionsAdapter.OnBindSuggestionCallback {

    companion object {
        const val ACTION_VIEW_FAVOURITES = "com.insiderser.android.movies.VIEW_FAVOURITES"
    }

    private val viewModel: MainViewModel by viewModelProvider {
        injector.getMainViewModel()
    }

    private val suggestionsObserver = Observer<List<SearchSuggestion>> { suggestions ->
        search_view.swapSuggestions(suggestions)
    }

    private var isSearchFragmentAdded = false

    private var isSearchAdditionPending = false
    private var isSearchRemovalPending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initViewModel()
        initFragments()

        if (savedInstanceState == null) {
            if (intent.action == ACTION_VIEW_FAVOURITES) {
                showFavourites()
            } else {
                showDiscoverMovies()
            }
        }
    }

    private fun initViews() {
        drawer_layout.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        initNavDrawer()
        initSearchView()
    }

    private fun initNavDrawer() {
        nav_view.setNavigationItemSelectedListener { menuItem ->
            drawer_layout.closeDrawer(GravityCompat.START)

            when (menuItem.itemId) {
                R.id.discover_movies -> {
                    showDiscoverMovies()
                    true
                }
                R.id.discover_tv_shows -> {
                    showDiscoverTvShows()
                    true
                }
                R.id.favourites -> {
                    showFavourites()
                    true
                }
                R.id.about -> {
                    openAbout()
                    true
                }
                else -> false
            }
        }
    }

    private fun initSearchView() {
        search_view.setOnApplyWindowInsetsListener { _, insets ->
            applyWindowInsets(insets)
            return@setOnApplyWindowInsetsListener insets
        }

        search_view.attachNavigationDrawerToMenuButton(drawer_layout)

        search_view.setOnLeftMenuClickListener(this)

        search_view.setOnSearchListener(this)

        search_view.setOnHomeActionClickListener {
            if (!hideSearchIfShown()) {
                finish()
            }
        }

        search_view.setOnFocusChangeListener(this)

        search_view.setOnBindSuggestionCallback(this)

        search_view.setOnQueryChangeListener { _, newQuery: String ->
            viewModel.query = newQuery
        }

        app_bar.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                    if (!search_view.isSearchBarFocused) {
                        search_view.translationY = verticalOffset.toFloat()
                    }
                })
    }

    private fun applyWindowInsets(insets: WindowInsets) {
        val statusBarHeight = insets.systemWindowInsetTop

        search_view.setPadding(search_view.paddingLeft,
                search_view.paddingTop + statusBarHeight,
                search_view.paddingRight,
                search_view.paddingBottom)
    }

    private fun initViewModel() {
        viewModel.isRefreshingSuggestions.observe(this@MainActivity, Observer { isRefreshing ->
            search_view.apply {
                if (isSearchBarFocused) {
                    if (isRefreshing) {
                        showProgress()
                    } else {
                        hideProgress()

                        updateSearchViewLeftActionMode()
                    }
                }
            }
        })
    }

    override fun onBackStackChanged() {
        val searchAdditionPending = isSearchAdditionPending
        val searchRemovalPending = isSearchRemovalPending

        val searchFragmentAdded = !isSearchFragmentAdded
        isSearchFragmentAdded = searchFragmentAdded

        if (searchAdditionPending) {
            check(searchFragmentAdded)
            check(!searchRemovalPending)

            isSearchAdditionPending = false
        } else {
            check(searchRemovalPending)
            check(!searchFragmentAdded)

            isSearchRemovalPending = false
        }

        if (!searchFragmentAdded) {
            hideSearchBarIfShown()
        }

        updateSearchViewLeftActionMode()
    }

    private fun initFragments() {
        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    private fun hideSearchIfShown(): Boolean {
        if ((isSearchFragmentAdded || isSearchAdditionPending) && !isSearchRemovalPending) {
            isSearchRemovalPending = true
            isSearchAdditionPending = false

            supportFragmentManager.popBackStack()

            return true
        }
        return false
    }

    private fun hideSearchBarIfShown(clearQuery: Boolean = true) {
        if (search_view.isSearchBarFocused) {
            if (clearQuery) {
                search_view.clearQuery()
                viewModel.query = ""
            }
            search_view.clearSuggestions()
            search_view.setSearchFocused(false)
        }
    }

    private fun showSearchIfNotShown() {
        if (!isSearchFragmentAdded && !isSearchAdditionPending) {
            isSearchAdditionPending = true

            supportFragmentManager.inTransaction {
                replace(R.id.fragment_container, SearchFragment(), SearchFragment.TAG)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
            }
        }
    }

    private fun showDiscoverMovies() {
        hideSearchIfShown()

        swapFragments(DiscoverFragment.TAG_MOVIES) {
            DiscoverFragment.newInstance(Type.MOVIE)
        }

        nav_view.setCheckedItem(R.id.discover_movies)
    }

    private fun showDiscoverTvShows() {
        hideSearchIfShown()

        swapFragments(DiscoverFragment.TAG_TV_SHOWS) {
            DiscoverFragment.newInstance(Type.TV_SHOW)
        }

        nav_view.setCheckedItem(R.id.discover_tv_shows)
    }

    private fun showFavourites() {
        hideSearchIfShown()

        swapFragments(FavouritesFragment.TAG) {
            FavouritesFragment()
        }

        nav_view.setCheckedItem(R.id.favourites)
    }

    private inline fun swapFragments(fragmentToShowTag: String,
            onCreateNewFragment: () -> Fragment) {

        val fragmentsToHideTags = mutableSetOf(DiscoverFragment.TAG_MOVIES,
                DiscoverFragment.TAG_TV_SHOWS, FavouritesFragment.TAG).also {
            it.remove(fragmentToShowTag)
        } as Collection<String>

        val fragmentsToHide = fragmentsToHideTags.mapNotNull {
            supportFragmentManager.findFragmentByTag(it)
        }
        val fragmentToShow = supportFragmentManager.findFragmentByTag(fragmentToShowTag)

        supportFragmentManager.inTransaction {
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

            fragmentsToHide.forEach { fragmentToHide ->
                hide(fragmentToHide)
            }

            if (fragmentToShow != null) {
                show(fragmentToShow)
            } else {
                add(R.id.fragment_container, onCreateNewFragment(), fragmentToShowTag)
            }
        }
    }

    private fun openDetails(movieId: Int, type: Type) {
        val movieDetailsIntent = BasicDetailsActivity.buildIntent(this, movieId, type)
        startActivity(movieDetailsIntent)
    }

    private fun openAbout() {
        val settingsIntent = Intent(this, AboutActivity::class.java)
        startActivity(settingsIntent)
    }

    override fun onSearchAction(currentQuery: String) {
        search_view.setSearchText(currentQuery.trim())

        viewModel.onQuerySubmit()

        if (currentQuery.isNotBlank()) {
            showSearchIfNotShown()
        } else {
            hideSearchIfShown()
        }
    }

    override fun onSuggestionClicked(searchSuggestion: SearchSuggestion) {
        if (searchSuggestion is PosterSearchSuggestion) {
            viewModel.onPosterSuggestionClicked()

            hideSearchIfShown()
            hideSearchBarIfShown()
        }

        when (searchSuggestion) {
            is PosterSearchSuggestion -> {
                val id = searchSuggestion.id
                openDetails(id, searchSuggestion.type)
            }
            is HistorySearchSuggestion -> {
                val query = searchSuggestion.query

                onSearchAction(query)

                hideSearchBarIfShown(clearQuery = false)
            }
        }
    }

    override fun onBindSuggestion(suggestionView: View,
            leftIcon: ImageView,
            textView: TextView,
            item: SearchSuggestion,
            itemPosition: Int) {
        when (item) {
            is PosterSearchSuggestion -> {
                onBindPosterSearchSuggestion(suggestionView, leftIcon, item)
            }
            is HistorySearchSuggestion -> {
                onBindHistorySuggestion(suggestionView, leftIcon, item)
            }
        }
    }

    private fun onBindPosterSearchSuggestion(suggestionView: View,
            leftIconView: ImageView,
            suggestion: PosterSearchSuggestion) {
        suggestionView.setOnLongClickListener(null)

        // TODO: network shouldn't be View's business
        val networkInfoProvider = injector.networkInfoProvider
        if (networkInfoProvider.isNetworkAvailable &&
                !networkInfoProvider.isNetworkMetered) {
            val posterPath = suggestion.posterPath

            val imageUri =
                    if (posterPath != null)
                        injector.tmdbUriBuilder.buildPosterImageUri(posterPath)
                    else null

            GlideApp.with(this@MainActivity)
                    .load(imageUri)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(leftIconView)
        } else {
            GlideUtils.clear(leftIconView)
        }
    }

    private fun onBindHistorySuggestion(suggestionView: View,
            leftIconView: ImageView,
            suggestion: HistorySearchSuggestion) {
        suggestionView.setOnLongClickListener {
            onHistorySuggestionLongClick(suggestion.query)
            true
        }

        GlideApp.with(this@MainActivity)
                .load(R.drawable.ic_history_white)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(leftIconView)
    }

    private fun onHistorySuggestionLongClick(query: String) {
        val dialogTitle = getString(R.string.query_suggestion_deletion_dialog_title, query)

        AlertDialog.Builder(this)
                .setTitle(dialogTitle)
                .setPositiveButton(R.string.delete) { dialog, _ ->
                    viewModel.removeQueryFromSuggestions(query)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    }

    override fun onFocus() {
        viewModel.searchSuggestions.observe(this@MainActivity, suggestionsObserver)
    }

    override fun onFocusCleared() {
        if (!isSearchFragmentAdded && !isSearchAdditionPending) {
            search_view.clearQuery()
        } else {
            search_view.setSearchText(viewModel.lastSubmittedQuery)
        }

        search_view.hideProgress()

        viewModel.searchSuggestions.removeObserver(suggestionsObserver)
    }

    private fun updateSearchViewLeftActionMode() {
        if (!search_view.isSearchBarFocused) {
            val leftActionMode =
                    if (isSearchFragmentAdded) LEFT_ACTION_MODE_SHOW_HOME
                    else LEFT_ACTION_MODE_SHOW_HAMBURGER

            search_view.setLeftActionMode(leftActionMode)
        }
    }

    override fun onMenuOpened() {
        drawer_layout.openDrawer(GravityCompat.START)
    }

    override fun onMenuClosed() = Unit

    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerVisible(GravityCompat.START) -> drawer_layout.closeDrawer(
                    GravityCompat.START)
            search_view.setSearchFocused(false) -> return
            hideSearchIfShown() -> return
            else -> super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(this::isSearchFragmentAdded.name, isSearchFragmentAdded)
        outState.putBoolean(this::isSearchAdditionPending.name, isSearchAdditionPending)
        outState.putBoolean(this::isSearchRemovalPending.name, isSearchRemovalPending)

        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        isSearchFragmentAdded = savedInstanceState.getBoolean(this::isSearchFragmentAdded.name)
        isSearchAdditionPending = savedInstanceState.getBoolean(this::isSearchAdditionPending.name)
        isSearchRemovalPending = savedInstanceState.getBoolean(this::isSearchRemovalPending.name)

        super.onRestoreInstanceState(savedInstanceState)
    }
}
