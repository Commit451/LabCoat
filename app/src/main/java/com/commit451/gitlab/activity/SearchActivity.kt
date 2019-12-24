package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import butterknife.*
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.SearchPagerAdapter
import com.commit451.jounce.Debouncer
import com.commit451.teleprinter.Teleprinter
import com.google.android.material.tabs.TabLayout
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

    @BindView(R.id.root)
    lateinit var root: View
    @BindView(R.id.tabs)
    lateinit var tabLayout: TabLayout
    @BindView(R.id.pager)
    lateinit var viewPager: ViewPager
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.search)
    lateinit var textSearch: EditText
    @BindView(R.id.clear)
    lateinit var buttonClear: View

    lateinit var adapterSearch: SearchPagerAdapter
    lateinit var mTeleprinter: Teleprinter

    private val debouncer = object : Debouncer<CharSequence>() {
        override fun onValueSet(value: CharSequence) {
            search()
        }
    }

    @OnClick(R.id.clear)
    fun onClearClick() {
        buttonClear.animate().alpha(0.0f).withEndAction { buttonClear.visibility = View.GONE }
        textSearch.text.clear()
        mTeleprinter.showKeyboard(textSearch)
        debouncer.cancel()
    }

    @OnEditorAction(R.id.search)
    fun onSearchEditorAction(): Boolean {
        if (textSearch.text.isNullOrEmpty()) {
            textSearch.setText("labcoat")
        }
        search()
        mTeleprinter.hideKeyboard()
        return false
    }

    @OnTextChanged(R.id.search)
    fun onSearchTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        ButterKnife.bind(this)
        mTeleprinter = Teleprinter(this)
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        adapterSearch = SearchPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = adapterSearch
        tabLayout.setupWithViewPager(viewPager)
    }

    fun search() {
        Timber.d("Searching")
        adapterSearch.searchQuery(textSearch.text.toString())
    }
}
