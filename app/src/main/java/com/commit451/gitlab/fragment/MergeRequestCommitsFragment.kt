package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryCommit
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.CommitViewHolder
import kotlinx.android.synthetic.main.fragment_merge_request_commits.*
import org.greenrobot.eventbus.Subscribe

/**
 * Like [CommitsFragment] but showing commits for a merge request
 */
class MergeRequestCommitsFragment : BaseFragment() {

    companion object {

        private const val KEY_PROJECT = "project"
        private const val KEY_MERGE_REQUEST = "merge_request"

        fun newInstance(project: Project, mergeRequest: MergeRequest): MergeRequestCommitsFragment {
            val fragment = MergeRequestCommitsFragment()
            val args = Bundle()
            args.putParcelable(KEY_PROJECT, project)
            args.putParcelable(KEY_MERGE_REQUEST, mergeRequest)
            fragment.arguments = args
            return fragment
        }
    }

    private var project: Project? = null
    private var mergeRequest: MergeRequest? = null

    private lateinit var adapter: BaseAdapter<RepositoryCommit, CommitViewHolder>
    private lateinit var loadHelper: LoadHelper<RepositoryCommit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments?.getParcelable(KEY_PROJECT)
        mergeRequest = arguments?.getParcelable(KEY_MERGE_REQUEST)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_merge_request_commits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                dividers = true,
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = {
                    gitLab.getMergeRequestCommits(project!!.id, mergeRequest!!.iid)
                },
                loadMore = {
                    gitLab.loadAnyList(it)
                }
        )

        loadData()
        App.bus().register(this)
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    override fun loadData() {
        loadHelper.load()
    }

    @Suppress("unused")
    @Subscribe
    fun onMergeRequestChangedEvent(event: MergeRequestChangedEvent) {
        if (mergeRequest?.iid == event.mergeRequest.id) {
            mergeRequest = event.mergeRequest
            loadData()
        }
    }
}
