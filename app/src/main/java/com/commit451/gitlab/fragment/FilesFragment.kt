package com.commit451.gitlab.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.BaseActivity
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.adapter.BreadcrumbAdapter
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.adapter.FileAdapter
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.getUrl
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryTreeObject
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.util.IntentUtil
import com.trello.rxlifecycle2.android.FragmentEvent
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.util.*

class FilesFragment : ButterKnifeFragment() {

    companion object {

        fun newInstance(): FilesFragment {
            return FilesFragment()
        }
    }

    @BindView(R.id.root) lateinit var root: View
    @BindView(R.id.swipe_layout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list) lateinit var list: RecyclerView
    @BindView(R.id.breadcrumb) lateinit var listBreadcrumbs: RecyclerView
    @BindView(R.id.message_text) lateinit var textMessage: TextView

    lateinit var adapterFiles: FileAdapter
    lateinit var adapterBreadcrumb: BreadcrumbAdapter

    var project: Project? = null
    lateinit var ref: String
    var currentPath = ""

    val filesAdapterListener = object : FileAdapter.Listener {
        override fun onFolderClicked(treeItem: RepositoryTreeObject) {
            loadData(currentPath + treeItem.name + "/")
        }

        override fun onFileClicked(treeItem: RepositoryTreeObject) {
            val path = currentPath + treeItem.name
            Navigator.navigateToFile(activity, project!!.id, path, ref)
        }

        override fun onCopyClicked(treeItem: RepositoryTreeObject) {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            // Creates a new text clip to put on the clipboard
            val clip = ClipData.newPlainText(treeItem.name, treeItem.getUrl(project!!, ref, currentPath).toString())
            clipboard.primaryClip = clip
            Snackbar.make(root, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT)
                    .show()
        }

        override fun onShareClicked(treeItem: RepositoryTreeObject) {
            IntentUtil.share(view!!, treeItem.getUrl(project!!, ref, currentPath))
        }

        override fun onOpenInBrowserClicked(treeItem: RepositoryTreeObject) {
            IntentUtil.openPage(activity as BaseActivity, treeItem.getUrl(project!!, ref, currentPath).toString(), App.get().currentAccount)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_files, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterFiles = FileAdapter(filesAdapterListener)
        list.layoutManager = LinearLayoutManager(activity)
        list.addItemDecoration(DividerItemDecoration(activity))
        list.adapter = adapterFiles

        adapterBreadcrumb = BreadcrumbAdapter()
        listBreadcrumbs.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        listBreadcrumbs.adapter = adapterBreadcrumb

        swipeRefreshLayout.setOnRefreshListener { loadData() }

        if (activity is ProjectActivity) {
            project = (activity as ProjectActivity).project
            ref = (activity as ProjectActivity).getRefRef()!!
            loadData("")
        } else {
            throw IllegalStateException("Incorrect parent activity")
        }

        App.bus().register(this)
    }

    override fun onBackPressed(): Boolean {
        if (adapterBreadcrumb.itemCount > 1) {
            val breadcrumb = adapterBreadcrumb.getValueAt(adapterBreadcrumb.itemCount - 2)
            if (breadcrumb != null) {
                breadcrumb.listener.onClick()
                return true
            }
        }

        return false
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    override fun loadData() {

        loadData(currentPath)
    }

    fun loadData(newPath: String) {

        if (project == null || ref.isNullOrEmpty()) {
            swipeRefreshLayout.isRefreshing = false
            return
        }

        swipeRefreshLayout.isRefreshing = true

        App.get().gitLab.getTree(project!!.id, ref, newPath)
                .setup(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(object : CustomSingleObserver<List<RepositoryTreeObject>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error_files)
                        adapterFiles.setData(null)
                        currentPath = newPath
                        updateBreadcrumbs()
                    }

                    override fun success(repositoryTreeObjects: List<RepositoryTreeObject>) {
                        swipeRefreshLayout.isRefreshing = false
                        if (!repositoryTreeObjects.isEmpty()) {
                            textMessage.visibility = View.GONE
                        } else {
                            Timber.d("No files found")
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_files_found)
                        }

                        adapterFiles.setData(repositoryTreeObjects)
                        list.scrollToPosition(0)
                        currentPath = newPath
                        updateBreadcrumbs()
                    }
                })
    }

    fun updateBreadcrumbs() {
        val breadcrumbs = ArrayList<BreadcrumbAdapter.Breadcrumb>()
        breadcrumbs.add(BreadcrumbAdapter.Breadcrumb(getString(R.string.root), object : BreadcrumbAdapter.Listener {
            override fun onClick() {
                loadData("")
            }
        }))

        var newPath = ""

        val segments = currentPath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (segment in segments) {
            if (segment.isEmpty()) {
                continue
            }

            newPath += segment + "/"

            val finalPath = newPath
            breadcrumbs.add(BreadcrumbAdapter.Breadcrumb(segment, object : BreadcrumbAdapter.Listener {
                override fun onClick() {
                    loadData(finalPath)
                }
            }))
        }

        adapterBreadcrumb.setData(breadcrumbs)
        listBreadcrumbs.scrollToPosition(adapterBreadcrumb.itemCount - 1)
    }


    @Suppress("unused")
    @Subscribe
    fun onProjectReload(event: ProjectReloadEvent) {
        project = event.project
        ref = event.branchName

        loadData("")
    }
}