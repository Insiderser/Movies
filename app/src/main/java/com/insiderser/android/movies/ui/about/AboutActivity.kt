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

package com.insiderser.android.movies.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.insiderser.android.movies.R
import com.insiderser.android.movies.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_about)

        binding.root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        binding.root.setOnApplyWindowInsetsListener { _, insets ->
            applyWindowInsets(insets)
            return@setOnApplyWindowInsetsListener insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.repo.setOnClickListener {
            openGithubRepo()
        }
    }

    private fun applyWindowInsets(insets: WindowInsets) {
        val statusBarHeight = insets.systemWindowInsetTop

        binding.root.apply {
            val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.apply {
                this.setMargins(leftMargin, statusBarHeight, rightMargin,
                        bottomMargin)
            }
            this.layoutParams = layoutParams
        }
    }

    private fun openGithubRepo() {
        val githubRepoUri = Uri.parse("https://github.com/Insiderser/Movies")
        val browserIntent = Intent(Intent.ACTION_VIEW, githubRepoUri)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (browserIntent.resolveActivity(packageManager) != null) {
            startActivity(browserIntent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
