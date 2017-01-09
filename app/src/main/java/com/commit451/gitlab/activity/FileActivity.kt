package com.commit451.gitlab.activity

import android.Manifest
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.annotation.IntDef
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.webkit.WebView

import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.RepositoryFile
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.rx.DecodeObservableFactory

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset

import butterknife.BindView
import butterknife.ButterKnife
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class FileActivity : BaseActivity() {

    companion object {

        private val REQUEST_PERMISSION_WRITE_STORAGE = 1337

        private val MAX_FILE_SIZE = (1024 * 1024).toLong()
        private val EXTRA_PROJECT_ID = "extra_project_id"
        private val EXTRA_PATH = "extra_path"
        private val EXTRA_REF = "extra_ref"

        const val OPTION_SAVE = 0
        const val OPTION_OPEN = 1

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(OPTION_SAVE.toLong(), OPTION_OPEN.toLong())
        annotation class Option

        fun newIntent(context: Context, projectId: Long, path: String, ref: String): Intent {
            val intent = Intent(context, FileActivity::class.java)
            intent.putExtra(EXTRA_PROJECT_ID, projectId)
            intent.putExtra(EXTRA_PATH, path)
            intent.putExtra(EXTRA_REF, ref)
            return intent
        }

        fun fileExtension(filename: String): String? {
            val extStart = filename.lastIndexOf(".") + 1
            if (extStart < 1) {
                return null
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

    var projectId: Long = 0
    var path: String? = null
    var ref: String? = null
    var repositoryFile: RepositoryFile? = null
    var fileName: String? = null
    var blob: ByteArray? = null
    @Option
    private var option: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)
        ButterKnife.bind(this)

        projectId = intent.getLongExtra(EXTRA_PROJECT_ID, -1)
        path = intent.getStringExtra(EXTRA_PATH)
        ref = intent.getStringExtra(EXTRA_REF)

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_open -> {
                    option = OPTION_OPEN
                    checkAccountPermission()
                    return@OnMenuItemClickListener true
                }
                R.id.action_save -> {
                    option = OPTION_SAVE
                    checkAccountPermission()
                    return@OnMenuItemClickListener true
                }
            }
            false
        })

        loadData()
    }

    private fun loadData() {
        progress.visibility = View.VISIBLE
        App.get().gitLab.getFile(projectId, path, ref)
                .compose(this.bindToLifecycle<RepositoryFile>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomSingleObserver<RepositoryFile>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        progress.visibility = View.GONE
                        Snackbar.make(root, R.string.file_load_error, Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun success(repositoryFile: RepositoryFile) {
                        progress.visibility = View.GONE
                        bindFile(repositoryFile)
                    }
                })
    }

    private fun bindFile(repositoryFile: RepositoryFile) {
        this.repositoryFile = repositoryFile
        fileName = repositoryFile.fileName
        toolbar.title = fileName
        if (repositoryFile.size > MAX_FILE_SIZE) {
            Snackbar.make(root, R.string.file_too_big, Snackbar.LENGTH_SHORT)
                    .show()
        } else {
            loadBlob(repositoryFile)
        }
    }

    private fun loadBlob(repositoryFile: RepositoryFile) {
        DecodeObservableFactory.newDecode(repositoryFile.content)
                .compose(this.bindToLifecycle<ByteArray>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomSingleObserver<ByteArray>() {

                    override fun error(t: Throwable) {
                        Snackbar.make(root, R.string.failed_to_load, Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun success(bytes: ByteArray) {
                        bindBlob(bytes)
                    }
                })
    }

    private fun bindBlob(blob: ByteArray) {
        this.blob = blob
        val content: String
        var mimeType: String? = null
        val extension = fileExtension(fileName!!)
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (mimeType != null) {
                mimeType = mimeType.toLowerCase()
            }
        }

        if (mimeType != null && mimeType.startsWith("image/")) {
            val imageURL = "data:" + mimeType + ";base64," + repositoryFile!!.content

            content = "<!DOCTYPE html>" +
                    "<html>" +
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
        toolbar.inflateMenu(R.menu.menu_file)
    }

    @TargetApi(23)
    private fun checkAccountPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (option == OPTION_SAVE) {
                saveBlob()
            } else {
                openFile()
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_WRITE_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_WRITE_STORAGE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (option == OPTION_SAVE) {
                        saveBlob()
                    } else {
                        openFile()
                    }
                }
            }
        }
    }

    private fun saveBlob(): File? {
        val state = Environment.getExternalStorageState()

        if (Environment.MEDIA_MOUNTED == state && blob != null) {
            val targetFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName!!)

            var outputStream: FileOutputStream? = null
            try {
                outputStream = FileOutputStream(targetFile)
                outputStream.write(blob!!)

                Snackbar.make(root, getString(R.string.file_saved), Snackbar.LENGTH_SHORT)
                        .show()

                return targetFile
            } catch (e: IOException) {
                Timber.e(e)
                Snackbar.make(root, getString(R.string.save_error), Snackbar.LENGTH_SHORT)
                        .show()
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close()
                    } catch (e: IOException) {
                        Timber.e(e)
                    }

                }
            }
        } else {
            Snackbar.make(root, getString(R.string.save_error), Snackbar.LENGTH_SHORT)
                    .show()
        }

        return null
    }

    private fun openFile() {
        val file = saveBlob()
        if (file == null) {
            Snackbar.make(root, getString(R.string.open_error), Snackbar.LENGTH_SHORT)
                    .show()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.data = Uri.fromFile(file)

        val extension = fileExtension(file.name)
        if (extension != null) {
            intent.setTypeAndNormalize(MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension))
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            Snackbar.make(root, getString(R.string.open_error), Snackbar.LENGTH_SHORT)
                    .show()
        } catch (e: SecurityException) {
            Timber.e(e)
            Snackbar.make(root, getString(R.string.open_error), Snackbar.LENGTH_SHORT).show()
        }

    }
}
