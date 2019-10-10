package com.commit451.gitlab.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.addendum.extraOrNull
import com.commit451.alakazam.fadeOut
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.ProjectPagerAdapter
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.fragment.BaseFragment
import com.commit451.gitlab.model.Ref
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.DeepLinker
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.IntentUtil
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import io.reactivex.Single
import timber.log.Timber

class ProjectActivity : BaseActivity() {

    companion object {

        private const val EXTRA_PROJECT = "extra_project"
        private const val EXTRA_PROJECT_ID = "extra_project_id"
        private const val EXTRA_PROJECT_NAMESPACE = "extra_project_namespace"
        private const val EXTRA_PROJECT_NAME = "extra_project_name"
        private const val EXTRA_PROJECT_SELECTION = "extra_project_selection"

        private const val STATE_REF = "ref"
        private const val STATE_PROJECT = "project"

        private const val REQUEST_BRANCH_OR_TAG = 1

        fun newIntent(context: Context, project: Project): Intent {
            val intent = Intent(context, ProjectActivity::class.java)
            intent.putExtra(EXTRA_PROJECT, project)
            return intent
        }

        fun newIntent(context: Context, projectId: String): Intent {
            val intent = Intent(context, ProjectActivity::class.java)
            intent.putExtra(EXTRA_PROJECT_ID, projectId)
            return intent
        }

        fun newIntent(context: Context, projectNamespace: String, projectName: String, projectSelection: DeepLinker.ProjectSelection): Intent {
            val intent = Intent(context, ProjectActivity::class.java)
            intent.putExtra(EXTRA_PROJECT_NAMESPACE, projectNamespace)
            intent.putExtra(EXTRA_PROJECT_NAME, projectName)
            intent.putExtra(EXTRA_PROJECT_SELECTION, projectSelection)
            return intent
        }
    }

    @BindView(R.id.root)
    lateinit var root: ViewGroup
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.tabs)
    lateinit var tabLayout: TabLayout
    @BindView(R.id.progress)
    lateinit var progress: View
    @BindView(R.id.pager)
    lateinit var viewPager: ViewPager

    var project: Project? = null
    var ref: Ref? = null

    private var adapter: ProjectPagerAdapter? = null

    private val projectSelection by extraOrNull<DeepLinker.ProjectSelection>(EXTRA_PROJECT_SELECTION)

    private val onMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.action_branch -> {
                if (project != null) {
                    Navigator.navigateToPickBranchOrTag(this@ProjectActivity, project!!.id, ref, REQUEST_BRANCH_OR_TAG)
                }
                return@OnMenuItemClickListener true
            }
            R.id.action_share -> {
                if (project != null) {
                    IntentUtil.share(root, Uri.parse(project!!.webUrl))
                }
                return@OnMenuItemClickListener true
            }
            R.id.action_copy_git_https -> {
                val url = project?.httpUrlToRepo
                if (url == null) {
                    Toast.makeText(this@ProjectActivity, R.string.failed_to_copy_to_clipboard, Toast.LENGTH_SHORT)
                            .show()
                } else {
                    copyToClipboard(url)
                }
                return@OnMenuItemClickListener true
            }
            R.id.action_copy_git_ssh -> {
                val url = project?.sshUrlToRepo
                if (url == null) {
                    Toast.makeText(this@ProjectActivity, R.string.failed_to_copy_to_clipboard, Toast.LENGTH_SHORT)
                            .show()
                } else {
                    copyToClipboard(url)
                }
                return@OnMenuItemClickListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.startingView = Prefs.STARTING_VIEW_PROJECTS
        setContentView(R.layout.activity_project)
        ButterKnife.bind(this)
        var project: Project? = intent.getParcelableExtra(EXTRA_PROJECT)

        if (savedInstanceState != null) {
            project = savedInstanceState.getParcelable(STATE_PROJECT)
            ref = savedInstanceState.getParcelable(STATE_REF)
        }
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.menu_project)
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener)

        if (project == null) {
            val projectId = intent.getStringExtra(EXTRA_PROJECT_ID)
            val projectNamespace = intent.getStringExtra(EXTRA_PROJECT_NAMESPACE)
            if (projectId != null) {
                loadProject(projectId)
            } else if (projectNamespace != null) {
                val projectName = intent.getStringExtra(EXTRA_PROJECT_NAME)
                loadProject(projectNamespace, projectName)
            } else {
                throw IllegalStateException("You did something wrong and now we don't know what project to load. :(")
            }
        } else {
            bindProject(project)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_BRANCH_OR_TAG -> if (resultCode == Activity.RESULT_OK) {
                ref = data?.getParcelableExtra(PickBranchOrTagActivity.EXTRA_REF)
                broadcastLoad()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_REF, ref)
        outState.putParcelable(STATE_PROJECT, project)
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.currentItem)
        if (fragment is BaseFragment) {
            if (fragment.onBackPressed()) {
                return
            }
        }

        super.onBackPressed()
    }

    override fun hasBrowsableLinks(): Boolean {
        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val selection = intent?.getSerializableExtra(EXTRA_PROJECT_SELECTION) as? DeepLinker.ProjectSelection
        selection?.let {
            val index = adapter?.indexForSelection(it)
            if (index != null) {
                viewPager.setCurrentItem(index, false)
            }
        }
    }

    private fun loadProject(projectId: String) {
        showProgress()
        loadProject(App.get().gitLab.getProject(projectId))
    }

    private fun loadProject(projectNamespace: String, projectName: String) {
        showProgress()
        loadProject(App.get().gitLab.getProject(projectNamespace, projectName))
    }

    private fun loadProject(observable: Single<Project>) {
        observable.with(this)
                .subscribe({
                    progress.fadeOut()
                    bindProject(it)
                }, {
                    Timber.e(it)
                    progress.fadeOut()
                    Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_INDEFINITE)
                            .show()
                })
    }

    private fun showProgress() {
        progress.alpha = 0.0f
        progress.visibility = View.VISIBLE
        progress.animate().alpha(1.0f)
    }

    private fun broadcastLoad() {
        App.bus().post(ProjectReloadEvent(project!!, ref!!.ref!!))
    }

    fun getRefRef(): String? {
        if (ref == null) {
            return null
        }
        return ref!!.ref
    }

    private fun bindProject(project: Project) {
        this.project = project
        if (ref == null) {
            ref = Ref(Ref.TYPE_BRANCH, project.defaultBranch)
        }
        toolbar.title = project.name
        toolbar.subtitle = project.namespace.name
        setupTabs()
    }

    private fun setupTabs() {
        val adapter = ProjectPagerAdapter(this, supportFragmentManager)
        this.adapter = adapter
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        projectSelection?.let {
            val index = adapter.indexForSelection(it)
            viewPager.setCurrentItem(index, false)
        }
    }

    private fun copyToClipboard(url: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Creates a new text clip to put on the clipboard
        val clip = ClipData.newPlainText(project!!.name, url)
        clipboard.setPrimaryClip(clip)
        Snackbar.make(root, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT)
                .show()
    }
}
