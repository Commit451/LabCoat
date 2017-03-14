package com.commit451.gitlab.activity

import `in`.uncod.android.bypass.Bypass
import `in`.uncod.android.bypass.ImageSpanClickListener
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.text.style.ImageSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.bypasspicassoimagegetter.BypassPicassoImageGetter
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import timber.log.Timber

/**
 * Displays the current users projects feed
 */
class DebugActivity : BaseActivity() {

    companion object {

        private val MARKDOWN = "![Image](https://gitlab.com/Commit451/LabCoat/raw/master/art/screenshot-1.png)"

        fun newIntent(context: Context): Intent {
            val intent = Intent(context, DebugActivity::class.java)
            return intent
        }
    }

    @BindView(R.id.root) lateinit var root: ViewGroup
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.text) lateinit var text: TextView

    lateinit var bypass: Bypass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        ButterKnife.bind(this)

        bypass = Bypass(this)
        bypass.setImageSpanClickListener(object : ImageSpanClickListener {
            override fun onImageClicked(p0: View?, p1: ImageSpan?, p2: String?) {
                Timber.d("Image clicked with url $p2")
                Snackbar.make(root, p2.toString(), Snackbar.LENGTH_LONG)
                        .show()
            }
        })

        toolbar.title = "Here Be Dragons"
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed()}

        text.text = bypass.markdownToSpannable(MARKDOWN,
                BypassPicassoImageGetter(text, App.get().picasso))
    }
}
