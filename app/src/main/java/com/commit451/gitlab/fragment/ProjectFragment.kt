package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.*
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryFile
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.InternalLinkMovementMethod
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.core.Single
import kotlinx.android.synthetic.main.fragment_project.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.util.*

/**
 * Shows the overview of the project
 */
class ProjectFragment : BaseFragment() {

    companion object {

        private const val README_TYPE_UNKNOWN = -1
        private const val README_TYPE_MARKDOWN = 0
        private const val README_TYPE_TEXT = 1
        private const val README_TYPE_HTML = 2
        private const val README_TYPE_NO_EXTENSION = 3

        fun newInstance(): ProjectFragment {
            return ProjectFragment()
        }
    }

    private var project: Project? = null
    private var branchName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_project, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        textOverview.movementMethod = InternalLinkMovementMethod(App.get().getAccount().serverUrl!!)

        swipeRefreshLayout.setOnRefreshListener { loadData() }

        rootStar.setOnClickListener {
            if (project != null) {
                App.get().gitLab.starProject(project!!.id)
                        .with(this)
                        .subscribe({
                            if (it.raw().code == 304) {
                                Snackbar.make(swipeRefreshLayout, R.string.project_already_starred, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.project_unstar) { unstarProject() }
                                        .show()
                            } else {
                                Snackbar.make(swipeRefreshLayout, R.string.project_starred, Snackbar.LENGTH_SHORT)
                                        .show()
                            }
                        }, {
                            Snackbar.make(swipeRefreshLayout, R.string.project_star_failed, Snackbar.LENGTH_SHORT)
                                    .show()
                        })
            }
        }
        rootFork.setOnClickListener {
            project?.let { project ->
                AlertDialog.Builder(baseActivty)
                        .setTitle(R.string.project_fork_title)
                        .setMessage(R.string.project_fork_message)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            App.get().gitLab.forkProject(project.id)
                                    .with(this)
                                    .subscribe({
                                        Snackbar.make(swipeRefreshLayout, R.string.project_forked, Snackbar.LENGTH_SHORT)
                                                .show()
                                    }, {
                                        Timber.e(it)
                                        Snackbar.make(swipeRefreshLayout, R.string.fork_failed, Snackbar.LENGTH_SHORT)
                                                .show()
                                    })
                        }
                        .show()
            }
        }
        textCreator.setOnClickListener {
            val project = project
            if (project != null) {
                val owner = project.owner
                if (owner != null) {
                    Navigator.navigateToUser(baseActivty, owner)
                } else {
                    Navigator.navigateToGroup(baseActivty, project.namespace!!.id)
                }
            }
        }
        if (activity is ProjectActivity) {
            project = (activity as ProjectActivity).project
            branchName = (activity as ProjectActivity).getRefRef()
            bindProject(project)
            loadData()
        } else {
            throw IllegalStateException("Incorrect parent activity")
        }
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    override fun loadData() {
        val project = project
        val branchName = branchName
        if (view != null && project != null && branchName != null) {
            swipeRefreshLayout.isRefreshing = true
            Single.defer {

                val readmeResult = ReadmeResult()
                val rootItems = App.get().gitLab.getTree(project.id, branchName, null)
                        .blockingGet()
                for (treeItem in rootItems) {
                    val treeItemName = treeItem.name
                    if (treeItemName != null && getReadmeType(treeItemName) != README_TYPE_UNKNOWN) {
                        //found a README
                        val repositoryFile = App.get().gitLab.getFile(project.id, treeItemName, branchName)
                                .blockingGet()
                        readmeResult.repositoryFile = repositoryFile
                        readmeResult.bytes = repositoryFile.content.base64Decode()
                                .blockingGet()
                        break
                    }
                }
                Single.just(readmeResult)
            }
                    .with(this)
                    .subscribe({
                        swipeRefreshLayout.isRefreshing = false
                        val repositoryFile = it.repositoryFile
                        val bytes = it.bytes
                        if (repositoryFile != null && bytes != null) {
                            val text = String(bytes)
                            when (getReadmeType(repositoryFile.fileName!!)) {
                                README_TYPE_MARKDOWN -> textOverview.setMarkdownText(text, project)
                                README_TYPE_HTML -> textOverview.text = text.formatAsHtml()
                                README_TYPE_TEXT -> textOverview.text = text
                                README_TYPE_NO_EXTENSION -> textOverview.text = text
                            }
                        } else {
                            textOverview.setText(R.string.no_readme_found)
                        }
                    }, {
                        Timber.e(it)
                        swipeRefreshLayout.isRefreshing = false
                        textOverview.setText(R.string.connection_error_readme)
                    })

        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun bindProject(project: Project?) {
        if (project == null) {
            return
        }

        if (project.belongsToGroup()) {
            textCreator.text = String.format(getString(R.string.created_by), project.namespace?.name)
        } else {
            textCreator.text = String.format(getString(R.string.created_by), project.owner!!.username)
        }
        textStarCount.text = project.starCount.toString()
        textForksCount.text = project.forksCount.toString()
    }

    private fun getReadmeType(filename: String): Int {
        when (filename.toLowerCase(Locale.ROOT)) {
            "readme.md" -> return README_TYPE_MARKDOWN
            "readme.html", "readme.htm" -> return README_TYPE_HTML
            "readme.txt" -> return README_TYPE_TEXT
            "readme" -> return README_TYPE_NO_EXTENSION
        }
        return README_TYPE_UNKNOWN
    }

    private fun unstarProject() {
        App.get().gitLab.unstarProject(project!!.id)
                .with(this)
                .subscribe({
                    Snackbar.make(swipeRefreshLayout, R.string.project_unstarred, Snackbar.LENGTH_SHORT)
                            .show()
                }, {
                    Timber.e(it)
                    Snackbar.make(swipeRefreshLayout, R.string.unstar_failed, Snackbar.LENGTH_SHORT)
                            .show()
                })
    }

    @Suppress("unused")
    @Subscribe
    fun onProjectReload(event: ProjectReloadEvent) {
        project = event.project
        branchName = event.branchName
        loadData()
    }

    class ReadmeResult {
        var bytes: ByteArray? = null
        var repositoryFile: RepositoryFile? = null
    }
}
