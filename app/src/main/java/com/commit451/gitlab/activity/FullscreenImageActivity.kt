package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.addendum.parceler.getParcelerParcelableExtra
import com.commit451.addendum.parceler.putParcelerParcelableExtra
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Project
import com.github.chrisbanes.photoview.PhotoView

/**
 * A full-screen activity that opens the clicked images
 */
class FullscreenImageActivity : BaseActivity() {

    companion object {

        private const val KEY_URL = "url"
        private const val KEY_PROJECT = "project"

        fun newIntent(context: Context, project: Project, url: String): Intent {
            val intent = Intent(context, FullscreenImageActivity::class.java)
            intent.putParcelerParcelableExtra(KEY_PROJECT, project)
            intent.putExtra(KEY_URL, url)
            return intent
        }
    }

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.photo_view)
    lateinit var photoView: PhotoView

    lateinit var project: Project

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)
        ButterKnife.bind(this)

        project = intent.getParcelerParcelableExtra(KEY_PROJECT)!!

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        photoView.scaleType = ImageView.ScaleType.FIT_CENTER

        var imageUrl: String = intent.getStringExtra(KEY_URL)
        if (imageUrl.startsWith("/")) {
            imageUrl = App.get().getAccount().serverUrl.toString() + project.pathWithNamespace + imageUrl
        }
        App.get().picasso
                .load(imageUrl)
                .into(photoView)
    }
}
