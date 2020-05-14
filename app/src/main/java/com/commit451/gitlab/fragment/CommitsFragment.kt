package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryCommit
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.CommitViewHolder
import kotlinx.android.synthetic.main.fragment_commits.*
import org.greenrobot.eventbus.Subscribe

class CommitsFragment : BaseFragment() {

    companion object {

        fun newInstance(): CommitsFragment {
            return CommitsFragment()
        }
    }

    private var project: Project? = null
    private var branchName: String? = null

    private lateinit var adapter: BaseAdapter<RepositoryCommit, CommitViewHolder>
    private lateinit var loadHelper: LoadHelper<RepositoryCommit>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_commits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    val viewHolder = CommitViewHolder.inflate(parent)
                    viewHolder.itemView.setOnClickListener {
                        val commit = adapter.items[viewHolder.adapterPosition]
                        Navigator.navigateToDiffActivity(baseActivty, project!!, commit)
                    }
                    viewHolder
                },
                onBindViewHolder = { viewHolder, _, item -> viewHolder.bind(item) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listCommits,
                baseAdapter = adapter,
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = {
                    gitLab.getCommits(project!!.id, branchName!!)
                },
                loadMore = {
                    gitLab.loadAnyList(it)
                }
        )

        listCommits.addItemDecoration(DividerItemDecoration(baseActivty))

        if (activity is ProjectActivity) {
            project = (activity as ProjectActivity).project
            branchName = (activity as ProjectActivity).getRefRef()
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
            return
        }

        loadHelper.load()
    }

    @Suppress("unused")
    @Subscribe
    fun onProjectReload(event: ProjectReloadEvent) {
        project = event.project
        branchName = event.branchName
        loadData()
    }
}
