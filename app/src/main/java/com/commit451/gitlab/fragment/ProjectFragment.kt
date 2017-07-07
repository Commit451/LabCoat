package com.commit451.gitlab.fragment

import `in`.uncod.android.bypass.Bypass
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.base64Decode
import com.commit451.gitlab.extension.formatAsHtml
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryFile
import com.commit451.gitlab.model.api.RepositoryTreeObject
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.util.BypassFactory
import com.commit451.gitlab.util.BypassImageGetterFactory
import com.commit451.gitlab.util.InternalLinkMovementMethod
import com.commit451.reptar.Optional

import com.trello.rxlifecycle2.android.FragmentEvent
import com.vdurmont.emoji.EmojiParser
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.functions.Function
import org.greenrobot.eventbus.Subscribe
import retrofit2.Response
import timber.log.Timber

/**
 * Shows the overview of the project
 */
class ProjectFragment : ButterKnifeFragment() {

    companion object {

        private val README_TYPE_UNKNOWN = -1
        private val README_TYPE_MARKDOWN = 0
        private val README_TYPE_TEXT = 1
        private val README_TYPE_HTML = 2
        private val README_TYPE_NO_EXTENSION = 3

        fun newInstance(): ProjectFragment {
            return ProjectFragment()
        }
    }

    @BindView(R.id.swipe_layout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.creator) lateinit var textCreator: TextView
    @BindView(R.id.star_count) lateinit var textStarCount: TextView
    @BindView(R.id.forks_count) lateinit var textForksCount: TextView
    @BindView(R.id.overview_text) lateinit var textOverview: TextView

    lateinit var bypass: Bypass

    var project: Project? = null
    var branchName: String? = null

    @OnClick(R.id.creator)
    fun onCreatorClick() {
        if (project != null) {
            if (project!!.belongsToGroup()) {
                Navigator.navigateToGroup(activity, project!!.namespace.id)
            } else {
                Navigator.navigateToUser(activity, project!!.owner)
            }
        }
    }

    @OnClick(R.id.root_fork)
    fun onForkClicked() {
        project?.let {
            AlertDialog.Builder(activity)
                    .setTitle(R.string.project_fork_title)
                    .setMessage(R.string.project_fork_message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        App.get().gitLab.forkProject(it.id)
                                .setup(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                                .subscribe(object : CustomSingleObserver<String>() {

                                    override fun error(t: Throwable) {
                                        Snackbar.make(swipeRefreshLayout, R.string.fork_failed, Snackbar.LENGTH_SHORT)
                                                .show()
                                    }

                                    override fun success(s: String) {
                                        Snackbar.make(swipeRefreshLayout, R.string.project_forked, Snackbar.LENGTH_SHORT)
                                                .show()
                                    }
                                })
                    }
                    .show()
        }
    }

    @OnClick(R.id.root_star)
    fun onStarClicked() {
        if (project != null) {
            App.get().gitLab.starProject(project!!.id)
                    .setup(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                    .subscribe(object : CustomSingleObserver<Response<Project>>() {

                        override fun error(t: Throwable) {
                            Snackbar.make(swipeRefreshLayout, R.string.project_star_failed, Snackbar.LENGTH_SHORT)
                                    .show()
                        }

                        override fun success(projectResponse: Response<Project>) {
                            if (projectResponse.raw().code() == 304) {
                                Snackbar.make(swipeRefreshLayout, R.string.project_already_starred, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.project_unstar) { unstarProject() }
                                        .show()
                            } else {
                                Snackbar.make(swipeRefreshLayout, R.string.project_starred, Snackbar.LENGTH_SHORT)
                                        .show()
                            }
                        }
                    })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_project, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        textOverview.movementMethod = InternalLinkMovementMethod(App.get().getAccount().serverUrl)

        swipeRefreshLayout.setOnRefreshListener { loadData() }

        if (activity is ProjectActivity) {
            project = (activity as ProjectActivity).project
            branchName = (activity as ProjectActivity).getRefRef()
            bypass = BypassFactory.create(context, project!!)
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
        if (view == null) {
            return
        }

        if (project == null || branchName.isNullOrEmpty()) {
            swipeRefreshLayout.isRefreshing = false
            return
        }

        swipeRefreshLayout.isRefreshing = true

        val result = ReadmeResult()
        App.get().gitLab.getTree(project!!.id, branchName!!, null)
                .flatMap(Function<List<RepositoryTreeObject>, SingleSource<Optional<RepositoryTreeObject>>> { repositoryTreeObjects ->
                    for (treeItem in repositoryTreeObjects) {
                        if (getReadmeType(treeItem.name) != README_TYPE_UNKNOWN) {
                            return@Function Single.just(Optional(treeItem))
                        }
                    }
                    Single.just(Optional.empty())
                })
                .flatMap(Function<Optional<RepositoryTreeObject>, SingleSource<Optional<RepositoryFile>>> { repositoryTreeObjectResult ->
                    if (repositoryTreeObjectResult.isPresent) {
                        val repositoryFile = App.get().gitLab.getFile(project!!.id, repositoryTreeObjectResult.get().name, branchName!!)
                                .blockingGet()
                        result.repositoryFile = repositoryFile
                        return@Function Single.just(Optional(repositoryFile))
                    }
                    Single.just(Optional.empty<RepositoryFile>())
                })
                .flatMap(Function<Optional<RepositoryFile>, SingleSource<ReadmeResult>> { repositoryFileResult ->
                    if (repositoryFileResult.isPresent) {
                        result.bytes = repositoryFileResult.get().content.base64Decode()
                                .blockingGet()
                        return@Function Single.just(result)
                    }
                    Single.just(result)
                })
                .setup(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(object : CustomSingleObserver<ReadmeResult>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        swipeRefreshLayout.isRefreshing = false
                        textOverview.setText(R.string.connection_error_readme)
                    }

                    override fun success(readmeResult: ReadmeResult) {
                        swipeRefreshLayout.isRefreshing = false
                        if (result.repositoryFile != null && result.bytes != null) {
                            var text = String(result.bytes!!)
                            when (getReadmeType(result.repositoryFile!!.fileName)) {
                                README_TYPE_MARKDOWN -> {
                                    text = EmojiParser.parseToUnicode(text)
                                    textOverview.text = bypass.markdownToSpannable(text,
                                            BypassImageGetterFactory.create(textOverview,
                                                    App.get().picasso,
                                                    App.get().getAccount().serverUrl.toString(),
                                                    project!!))
                                }
                                README_TYPE_HTML -> textOverview.text = text.formatAsHtml()
                                README_TYPE_TEXT -> textOverview.text = text
                                README_TYPE_NO_EXTENSION -> textOverview.text = text
                            }
                        } else {
                            textOverview.setText(R.string.no_readme_found)
                        }
                    }
                })
    }

    fun bindProject(project: Project?) {
        if (project == null) {
            return
        }

        if (project.belongsToGroup()) {
            textCreator.text = String.format(getString(R.string.created_by), project.namespace.name)
        } else {
            textCreator.text = String.format(getString(R.string.created_by), project.owner.username)
        }
        textStarCount.text = project.starCount.toString()
        textForksCount.text = project.forksCount.toString()
    }

    fun getReadmeType(filename: String): Int {
        when (filename.toLowerCase()) {
            "readme.md" -> return README_TYPE_MARKDOWN
            "readme.html", "readme.htm" -> return README_TYPE_HTML
            "readme.txt" -> return README_TYPE_TEXT
            "readme" -> return README_TYPE_NO_EXTENSION
        }
        return README_TYPE_UNKNOWN
    }

    fun unstarProject() {
        App.get().gitLab.unstarProject(project!!.id)
                .setup(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(object : CustomSingleObserver<Project>() {

                    override fun error(t: Throwable) {
                        Snackbar.make(swipeRefreshLayout, R.string.unstar_failed, Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun success(project: Project) {
                        Snackbar.make(swipeRefreshLayout, com.commit451.gitlab.R.string.project_unstarred, Snackbar.LENGTH_SHORT)
                                .show()
                    }
                })
    }

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
