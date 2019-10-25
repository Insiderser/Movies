/*
 * Copyright (c) 2019 Oleksandr Bezushko
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.insiderser.android.movies.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.FloatingSearchView.LEFT_ACTION_MODE_SHOW_HOME
import com.arlib.floatingsearchview.FloatingSearchView.LEFT_ACTION_MODE_SHOW_SEARCH
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.AppBarLayout
import com.insiderser.android.movies.R
import com.insiderser.android.movies.api.tmdb.TmdbUriBuilder
import com.insiderser.android.movies.di.injector
import com.insiderser.android.movies.glide.GlideApp
import com.insiderser.android.movies.glide.GlideUtils
import com.insiderser.android.movies.model.MovieSearchSuggestion
import com.insiderser.android.movies.model.PastQuerySearchSuggestion
import com.insiderser.android.movies.ui.about.AboutActivity
import com.insiderser.android.movies.ui.details.basic.BasicMovieDetailsActivity
import com.insiderser.android.movies.utils.extentions.hasTaskParents
import com.insiderser.android.movies.utils.extentions.inTransaction
import com.insiderser.android.movies.utils.extentions.viewModelProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    
    companion object {
        const val ACTION_VIEW_FAVOURITES = "com.insiderser.android.movies.VIEW_FAVOURITES"
        
        private const val NAV_VIEW_VISIBILITY_KEY = "nav_view_visibility"
    }
    
    private val viewModel: MainViewModel by viewModelProvider {
        injector.getMainViewModel()
    }
    
    private var isSearchFragmentAdded = false
    
    private var isSearchAdditionPending = false
    private var isSearchRemovalPending = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        nav_view.setOnNavigationItemSelectedListener { menuItem ->
            hideSearchIfShown()
            
            when(menuItem.itemId) {
                R.id.discover -> {
                    swapFragments(fragmentFromTag = FavouritesFragment.TAG,
                            fragmentToTag = DiscoverFragment.TAG,
                            onCreateNewFragment = { DiscoverFragment() })
                    true
                }
                R.id.favourites -> {
                    swapFragments(fragmentFromTag = DiscoverFragment.TAG,
                            fragmentToTag = FavouritesFragment.TAG,
                            onCreateNewFragment = { FavouritesFragment() })
                    true
                }
                else -> false
            }
        }
        
        search_view.setOnSearchListener(object : FloatingSearchView.OnSearchListener {
            override fun onSearchAction(currentQuery: String) {
                search_view.setSearchText(currentQuery.trim())
                
                viewModel.onQuerySubmit()
                
                if(currentQuery.isNotBlank()) {
                    showSearchIfNotShown()
                } else {
                    hideSearchIfShown()
                }
            }
            
            override fun onSuggestionClicked(searchSuggestion: SearchSuggestion) {
                when(searchSuggestion) {
                    is MovieSearchSuggestion -> {
                        viewModel.onMovieSuggestionClicked()
                        
                        val movieId = searchSuggestion.id
                        openMovieDetails(movieId)
                        
                        if(isSearchFragmentAdded || isSearchAdditionPending) {
                            hideSearchIfShown()
                        } else {
                            hideSearchBar()
                        }
                    }
                    is PastQuerySearchSuggestion -> {
                        val query = searchSuggestion.query
                        search_view.setSearchText(query)
                        onSearchAction(query)
                        
                        hideSearchBar(clearQuery = false)
                    }
                }
            }
        })
        
        search_view.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.sort_by -> showSortByDialog()
                R.id.about -> openAbout()
            }
        }
        
        search_view.setOnHomeActionClickListener {
            hideSearchIfShown()
        }
        
        search_view.setOnFocusChangeListener(object : FloatingSearchView.OnFocusChangeListener {
            
            private val suggestionsObserver = Observer<List<SearchSuggestion>> { suggestions ->
                search_view.swapSuggestions(suggestions)
            }
            
            override fun onFocus() {
                nav_view.visibility = GONE
                
                viewModel.searchSuggestions.observe(this@MainActivity, suggestionsObserver)
            }
            
            override fun onFocusCleared() {
                if(! isSearchFragmentAdded && ! isSearchAdditionPending) {
                    nav_view.visibility = VISIBLE
                    search_view.clearQuery()
                } else {
                    search_view.setSearchText(viewModel.lastSubmittedQuery)
                }
                
                viewModel.searchSuggestions.removeObserver(suggestionsObserver)
            }
        })
        
        search_view.setOnBindSuggestionCallback { suggestionView, leftIcon, _, item, _ ->
            when(item) {
                is MovieSearchSuggestion -> {
                    suggestionView.setOnLongClickListener(null)
                    
                    val networkInfoProvider = injector.networkInfoProvider
                    if(networkInfoProvider.isNetworkAvailable &&
                            ! networkInfoProvider.isNetworkMetered) {
                        val posterPath = item.posterPath
                        
                        val imageUri =
                                if(posterPath != null)
                                    TmdbUriBuilder.buildPosterImageUri(applicationContext,
                                            posterPath)
                                else null
                        
                        GlideApp.with(this@MainActivity)
                                .load(imageUri)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .into(leftIcon)
                    } else {
                        GlideUtils.clear(leftIcon)
                    }
                }
                is PastQuerySearchSuggestion -> {
                    suggestionView.setOnLongClickListener {
                        onPastQuerySuggestionLongClick(item.query)
                        true
                    }
                    
                    GlideApp.with(this@MainActivity)
                            .load(R.drawable.ic_history_white)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(leftIcon)
                }
            }
        }
        
        search_view.setOnQueryChangeListener { _, newQuery: String ->
            viewModel.query = newQuery
        }
        
        app_bar.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                    search_view.translationY = verticalOffset.toFloat()
                })
        
        viewModel.isRefreshingSuggestions.observe(this@MainActivity, Observer { isRefreshing ->
            search_view.apply {
                if(isRefreshing)
                    showProgress()
                else
                    hideProgress()
            }
        })
        
        supportFragmentManager.addOnBackStackChangedListener {
            val searchAdditionPending = isSearchAdditionPending
            val searchRemovalPending = isSearchRemovalPending
            
            val searchFragmentAdded = ! isSearchFragmentAdded
            isSearchFragmentAdded = searchFragmentAdded
            
            if(searchAdditionPending) {
                check(searchFragmentAdded)
                check(! searchRemovalPending)
                
                isSearchAdditionPending = false
            } else {
                check(searchRemovalPending)
                check(! searchFragmentAdded)
                
                isSearchRemovalPending = false
            }
            
            if(searchFragmentAdded) {
                nav_view.visibility = GONE
                
                val leftActionMode = if(hasTaskParents) LEFT_ACTION_MODE_SHOW_HOME else LEFT_ACTION_MODE_SHOW_SEARCH
                
                search_view.setLeftActionMode(leftActionMode)
            } else {
                nav_view.visibility = VISIBLE
                search_view.setLeftActionMode(LEFT_ACTION_MODE_SHOW_SEARCH)
                
                hideSearchBar()
            }
        }
        
        if(savedInstanceState == null) {
            if(intent.action == ACTION_VIEW_FAVOURITES) {
                showFavourites()
            } else {
                showDiscover()
            }
        }
    }
    
    private fun hideSearchIfShown() {
        if((isSearchFragmentAdded || isSearchAdditionPending) && ! isSearchRemovalPending) {
            isSearchRemovalPending = true
            isSearchAdditionPending = false
            
            supportFragmentManager.popBackStack()
        }
    }
    
    private fun hideSearchBar(clearQuery: Boolean = true) {
        search_view.also { searchView ->
            if(clearQuery) {
                searchView.clearQuery()
                viewModel.query = ""
            }
            searchView.clearSuggestions()
            searchView.setSearchFocused(false)
        }
    }
    
    private fun showSearchIfNotShown() {
        if(! isSearchFragmentAdded && ! isSearchAdditionPending) {
            isSearchAdditionPending = true
            
            supportFragmentManager.inTransaction {
                replace(R.id.fragment_container, SearchFragment(), SearchFragment.TAG)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
            }
        }
    }
    
    private fun showDiscover() {
        nav_view.selectedItemId = R.id.discover
    }
    
    private fun showFavourites() {
        nav_view.selectedItemId = R.id.favourites
    }
    
    private inline fun swapFragments(fragmentFromTag: String,
            fragmentToTag: String,
            @IdRes fragmentContainerId: Int = R.id.fragment_container,
            onCreateNewFragment: () -> Fragment) {
        
        val fragmentFrom = supportFragmentManager.findFragmentByTag(fragmentFromTag)
        val fragmentTo = supportFragmentManager.findFragmentByTag(fragmentToTag)
        
        supportFragmentManager.inTransaction {
            if(fragmentFrom != null) {
                hide(fragmentFrom)
            }
            
            if(fragmentTo != null) {
                show(fragmentTo)
            } else {
                add(fragmentContainerId, onCreateNewFragment(), fragmentToTag)
            }
        }
    }
    
    private fun openMovieDetails(movieId: Int) {
        val movieDetailsIntent = BasicMovieDetailsActivity.buildIntent(this@MainActivity, movieId)
        startActivity(movieDetailsIntent)
    }
    
    private fun openAbout() {
        val settingsIntent = Intent(this, AboutActivity::class.java)
        startActivity(settingsIntent)
    }
    
    private fun showSortByDialog() {
        SortByDialogFragment().show(supportFragmentManager)
    }
    
    private fun onPastQuerySuggestionLongClick(query: String) {
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
    
    override fun onBackPressed() {
        if(! search_view.setSearchFocused(false)) {
            if(isSearchFragmentAdded || isSearchAdditionPending) {
                hideSearchIfShown()
            } else {
                super.onBackPressed()
            }
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(NAV_VIEW_VISIBILITY_KEY, nav_view.visibility)
        
        outState.putBoolean(::isSearchFragmentAdded.name, isSearchFragmentAdded)
        outState.putBoolean(::isSearchAdditionPending.name, isSearchAdditionPending)
        outState.putBoolean(::isSearchRemovalPending.name, isSearchRemovalPending)
        
        super.onSaveInstanceState(outState)
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        nav_view.visibility = savedInstanceState.getInt(NAV_VIEW_VISIBILITY_KEY, VISIBLE)
        
        isSearchFragmentAdded = savedInstanceState.getBoolean(::isSearchFragmentAdded.name)
        isSearchAdditionPending = savedInstanceState.getBoolean(::isSearchAdditionPending.name)
        isSearchRemovalPending = savedInstanceState.getBoolean(::isSearchRemovalPending.name)
        
        super.onRestoreInstanceState(savedInstanceState)
    }
}