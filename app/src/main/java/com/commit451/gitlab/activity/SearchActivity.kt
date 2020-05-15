package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.SearchPagerAdapter
import com.commit451.jounce.Debouncer
import com.commit451.teleprinter.Teleprinter
import kotlinx.android.synthetic.main.activity_search.*
import timber.log.Timber


/**
 * Search for :allthethings:
 */
class SearchActivity : BaseActivity() {

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, SearchActivity::class.java)
        }
    }

    private lateinit var adapterSearch: SearchPagerAdapter
    private lateinit var teleprinter: Teleprinter

    private val debouncer = object : Debouncer<CharSequence>() {
        override fun onValueSet(value: CharSequence) {
            search()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        teleprinter = Teleprinter(this)
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        adapterSearch = SearchPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = adapterSearch
        tabLayout.setupWithViewPager(viewPager)
        textSearch.requestFocus()
        buttonClear.setOnClickListener {
            buttonClear.animate().alpha(0.0f).withEndAction { buttonClear.visibility = View.GONE }
            textSearch.text.clear()
            teleprinter.showKeyboard(textSearch)
            debouncer.cancel()
        }
        textSearch.setOnEditorActionListener { _, _, _ ->
            if (textSearch.text.isNullOrEmpty()) {
                textSearch.setText("labcoat")
            }
            search()
            teleprinter.hideKeyboard()
            false
        }
        textSearch.addTextChangedListener(onTextChanged = { s, _, before, count ->
            if (s.isNullOrEmpty()) {
                buttonClear.animate().alpha(0.0f).withEndAction { buttonClear.visibility = View.GONE }
            } else if (count == 1) {
                buttonClear.visibility = View.VISIBLE
                buttonClear.animate().alpha(1.0f)
            }
            if (s != null && s.length > 3) {
                Timber.d("Posting new future search")
                debouncer.value = s
            }
            //This means they are backspacing
            if (before > count) {
                Timber.d("Removing future search")
                debouncer.cancel()
            }
        }
        )
    }

    private fun search() {
        val query = textSearch.text.toString()
        Timber.d("Searching $query")
        adapterSearch.searchQuery(query)
    }
}
