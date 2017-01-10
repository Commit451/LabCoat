package com.commit451.gitlab.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.alakazam.HideRunnable
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.ProjectSectionsPagerAdapter
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.fragment.BaseFragment
import com.commit451.gitlab.model.Ref
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.util.IntentUtil
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.parceler.Parcels
import timber.log.Timber

class ProjectActivity : BaseActivity() {

    companion object {

        private val EXTRA_PROJECT = "extra_project"
        private val EXTRA_PROJECT_ID = "extra_project_id"
        private val EXTRA_PROJECT_NAMESPACE = "extra_project_namespace"
        private val EXTRA_PROJECT_NAME = "extra_project_name"

        private val STATE_REF = "ref"
        private val STATE_PROJECT = "project"

        private val REQUEST_BRANCH_OR_TAG = 1

        fun newIntent(context: Context, project: Project): Intent {
            val intent = Intent(context, ProjectActivity::class.java)
            intent.putExtra(EXTRA_PROJECT, Parcels.wrap(project))
            return intent
        }

        fun newIntent(context: Context, projectId: String): Intent {
            val intent = Intent(context, ProjectActivity::class.java)
            intent.putExtra(EXTRA_PROJECT_ID, projectId)
            return intent
        }

        fun newIntent(context: Context, projectNamespace: String, projectName: String): Intent {
            val intent = Intent(context, ProjectActivity::class.java)
            intent.putExtra(EXTRA_PROJECT_NAMESPACE, projectNamespace)
            intent.putExtra(EXTRA_PROJECT_NAME, projectName)
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
        internal set
    var ref: Ref? = null
        internal set

    val onMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.action_branch -> {
                if (project != null) {
                    Navigator.navigateToPickBranchOrTag(this@ProjectActivity, project!!.id, ref, REQUEST_BRANCH_OR_TAG)
                }
                return@OnMenuItemClickListener true
            }
            R.id.action_share -> {
                if (project != null) {
                    IntentUtil.share(root, project!!.webUrl)
                }
                return@OnMenuItemClickListener true
            }
            R.id.action_copy_git_https -> {
                if (project == null || project!!.httpUrlToRepo == null) {
                    Toast.makeText(this@ProjectActivity, R.string.failed_to_copy_to_clipboard, Toast.LENGTH_SHORT)
                            .show()
                } else {
                    copyToClipboard(project!!.httpUrlToRepo)
                }
                return@OnMenuItemClickListener true
            }
            R.id.action_copy_git_ssh -> {
                if (project == null || project!!.httpUrlToRepo == null) {
                    Toast.makeText(this@ProjectActivity, R.string.failed_to_copy_to_clipboard, Toast.LENGTH_SHORT)
                            .show()
                } else {
                    copyToClipboard(project!!.sshUrlToRepo)
                }
                return@OnMenuItemClickListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.get().prefs.setStartingView(Prefs.STARTING_VIEW_PROJECTS)
        setContentView(R.layout.activity_project)
        ButterKnife.bind(this)
        var project: Project? = Parcels.unwrap<Project>(intent.getParcelableExtra<Parcelable>(EXTRA_PROJECT))

        if (savedInstanceState != null) {
            project = Parcels.unwrap<Project>(savedInstanceState.getParcelable<Parcelable>(STATE_PROJECT))
            ref = Parcels.unwrap<Ref>(savedInstanceState.getParcelable<Parcelable>(STATE_REF))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_BRANCH_OR_TAG -> if (resultCode == Activity.RESULT_OK) {
                ref = Parcels.unwrap<Ref>(data.getParcelableExtra<Parcelable>(PickBranchOrTagActivity.EXTRA_REF))
                broadcastLoad()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_REF, Parcels.wrap<Ref>(ref))
        outState.putParcelable(STATE_PROJECT, Parcels.wrap<Project>(project))
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

    fun loadProject(projectId: String) {
        showProgress()
        loadProject(App.get().gitLab.getProject(projectId))
    }

    fun loadProject(projectNamespace: String, projectName: String) {
        showProgress()
        loadProject(App.get().gitLab.getProject(projectNamespace, projectName))
    }

    fun loadProject(observable: Single<Project>) {
        observable.compose(this.bindToLifecycle<Project>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomSingleObserver<Project>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        progress.animate()
                                .alpha(0.0f)
                                .withEndAction(HideRunnable(progress))
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun success(project: Project) {
                        progress.animate()
                                .alpha(0.0f)
                                .withEndAction(HideRunnable(progress))
                        bindProject(project)
                    }
                })
    }

    fun showProgress() {
        progress.alpha = 0.0f
        progress.visibility = View.VISIBLE
        progress.animate().alpha(1.0f)
    }

    fun broadcastLoad() {
        App.bus().post(ProjectReloadEvent(project!!, ref!!.ref))
    }

    fun getRefRef(): String? {
        if (ref == null) {
            return null
        }
        return ref!!.ref
    }

    fun bindProject(project: Project) {
        this.project = project
        if (ref == null) {
            ref = Ref(Ref.TYPE_BRANCH, this.project!!.defaultBranch)
        }
        toolbar.title = this.project!!.name
        toolbar.subtitle = this.project!!.namespace.name
        setupTabs()
    }

    fun setupTabs() {
        val projectSectionsPagerAdapter = ProjectSectionsPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = projectSectionsPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    fun copyToClipboard(url: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Creates a new text clip to put on the clipboard
        val clip = ClipData.newPlainText(project!!.name, url)
        clipboard.primaryClip = clip
        Snackbar.make(root, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT)
                .show()
    }
}
