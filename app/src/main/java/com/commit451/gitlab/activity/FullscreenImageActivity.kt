package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import coil.api.load
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Project
import kotlinx.android.synthetic.main.activity_fullscreen_image.*

/**
 * A full-screen activity that opens the clicked images
 */
class FullscreenImageActivity : BaseActivity() {

    companion object {

        private const val KEY_URL = "url"
        private const val KEY_PROJECT = "project"

        fun newIntent(context: Context, project: Project, url: String): Intent {
            val intent = Intent(context, FullscreenImageActivity::class.java)
            intent.putExtra(KEY_PROJECT, project)
            intent.putExtra(KEY_URL, url)
            return intent
        }
    }

    private lateinit var project: Project

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        project = intent.getParcelableExtra(KEY_PROJECT)!!

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        photoView.scaleType = ImageView.ScaleType.FIT_CENTER

        var imageUrl: String = intent.getStringExtra(KEY_URL)!!
        if (imageUrl.startsWith("/")) {
            imageUrl = App.get().getAccount().serverUrl.toString() + project.pathWithNamespace + imageUrl
        }
        photoView.load(imageUrl)
    }
}
