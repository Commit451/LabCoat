package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.webkit.WebView
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.addendum.extra
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.base64Decode
import com.commit451.gitlab.extension.getUrl
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryFile
import com.commit451.gitlab.util.IntentUtil
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.nio.charset.Charset

class FileActivity : BaseActivity() {

    companion object {

        private const val MAX_FILE_SIZE = (1024 * 1024).toLong()
        private const val KEY_PROJECT = "project"
        private const val KEY_PATH = "path"
        private const val KEY_BRANCH = "branch"

        fun newIntent(context: Context, project: Project, path: String, branch: String): Intent {
            val intent = Intent(context, FileActivity::class.java)
            intent.putExtra(KEY_PROJECT, project)
            intent.putExtra(KEY_PATH, path)
            intent.putExtra(KEY_BRANCH, branch)
            return intent
        }

        fun fileExtension(filename: String): String {
            val extStart = filename.lastIndexOf(".") + 1
            if (extStart < 1) {
                return ""
            }

            return filename.substring(extStart)
        }
    }

    @BindView(R.id.root)
    lateinit var root: ViewGroup
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.file_blob)
    lateinit var webViewFileBlob: WebView
    @BindView(R.id.progress)
    lateinit var progress: View

    private val project by extra<Project>(KEY_PROJECT)
    private val path by extra<String>(KEY_PATH)
    private val branch by extra<String>(KEY_BRANCH)
    private var repositoryFile: RepositoryFile? = null
    private var fileName: String? = null
    private var blob: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)
        ButterKnife.bind(this)

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.inflateMenu(R.menu.browser)
        toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.action_browser -> {
                    openInBrowser()
                }
            }
            false
        }

        loadData()
    }

    override fun hasBrowsableLinks() = true

    private fun loadData() {
        progress.visibility = View.VISIBLE
        App.get().gitLab.getFile(project.id, path, branch)
                .with(this)
                .subscribe({
                    progress.visibility = View.GONE
                    bindFile(it)
                }, {
                    Timber.e(it)
                    progress.visibility = View.GONE
                    Snackbar.make(root, R.string.file_load_error, Snackbar.LENGTH_SHORT)
                            .show()
                })
    }

    private fun bindFile(repositoryFile: RepositoryFile) {
        this.repositoryFile = repositoryFile
        fileName = repositoryFile.fileName
        toolbar.title = fileName
        if (repositoryFile.size > MAX_FILE_SIZE) {
            Snackbar.make(root, R.string.file_too_big, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.action_open_in_browser) {
                        openInBrowser()
                    }
                    .show()
        } else {
            loadBlob(repositoryFile)
        }
    }

    private fun loadBlob(repositoryFile: RepositoryFile) {
        repositoryFile.content.base64Decode()
                .with(this)
                .subscribe({
                    bindBlob(it)
                }, {
                    Snackbar.make(root, R.string.failed_to_load, Snackbar.LENGTH_SHORT)
                            .setAction(R.string.action_retry) {
                                loadBlob(repositoryFile)
                            }
                            .show()
                })
    }

    private fun bindBlob(blob: ByteArray) {
        this.blob = blob
        val content: String
        var mimeType: String?
        val extension = fileExtension(fileName!!)
        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (mimeType != null) {
            mimeType = mimeType.toLowerCase()
        }

        if (mimeType != null && mimeType.startsWith("image/")) {
            val imageURL = "data:" + mimeType + ";base64," + repositoryFile!!.content

            content = "<!DOCTYPE html>" +
                    "<html>" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "<body>" +
                    "<img style=\"width: 100%;\" src=\"" + imageURL + "\">" +
                    "</body>" +
                    "</html>"
        } else {
            val text = String(this.blob!!, Charset.forName("UTF-8"))

            content = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<link href=\"github.css\" rel=\"stylesheet\" />" +
                    "</head>" +
                    "<body>" +
                    "<pre><code>" +
                    Html.escapeHtml(text) +
                    "</code></pre>" +
                    "<script src=\"highlight.pack.js\"></script>" +
                    "<script>hljs.initHighlightingOnLoad();</script>" +
                    "</body>" +
                    "</html>"
        }

        webViewFileBlob.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf8", null)
    }

    private fun openInBrowser() {
        repositoryFile?.let {file->
            IntentUtil.openPage(this, file.getUrl(project, branch, path), App.get().currentAccount)
        }
    }
}
