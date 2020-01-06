package com.commit451.gitlab.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.PickBranchOrTagActivity
import com.commit451.gitlab.adapter.BranchAdapter
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.Ref
import com.commit451.gitlab.model.api.Branch
import com.commit451.gitlab.util.OnScrollLoadMoreListener
import kotlinx.android.synthetic.main.fragment_pick_branch.*
import kotlinx.android.synthetic.main.progress.*
import timber.log.Timber

/**
 * Pick a branch, any branch
 */
class PickBranchFragment : BaseFragment() {

    companion object {

        private const val EXTRA_PROJECT_ID = "project_id"
        private const val EXTRA_REF = "ref"

        fun newInstance(projectId: Long, ref: Ref?): PickBranchFragment {
            val fragment = PickBranchFragment()
            val args = Bundle()
            args.putLong(EXTRA_PROJECT_ID, projectId)
            args.putParcelable(EXTRA_REF, ref)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var adapterBranches: BranchAdapter

    private var projectId: Long = 0

    private var nextPageUrl: String? = null
    private var loading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectId = arguments?.getLong(EXTRA_PROJECT_ID)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pick_branch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val existingRef = arguments?.getParcelable<Ref>(EXTRA_REF)!!
        adapterBranches = BranchAdapter(existingRef, object : BranchAdapter.Listener {
            override fun onBranchClicked(entry: Branch) {
                val data = Intent()
                val ref = Ref(Ref.TYPE_BRANCH, entry.name)
                data.putExtra(PickBranchOrTagActivity.EXTRA_REF, ref)
                activity?.setResult(Activity.RESULT_OK, data)
                activity?.finish()
            }
        })
        val layoutManager = LinearLayoutManager(activity)
        listProjects.layoutManager = layoutManager
        listProjects.adapter = adapterBranches
        listProjects.addOnScrollListener(OnScrollLoadMoreListener(layoutManager, {
            !loading && nextPageUrl != null
        }, {
            loadMore()
        }))

        loadData()
    }

    override fun loadData() {
        loading = true
        progress.visibility = View.VISIBLE
        textMessage.visibility = View.GONE

        App.get().gitLab.getBranches(projectId)
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    nextPageUrl = it.paginationData.next
                    progress.visibility = View.GONE
                    adapterBranches.setEntries(it.body)
                }, {
                    Timber.e(it)
                    progress.visibility = View.GONE
                    textMessage.visibility = View.VISIBLE
                })
    }

    fun loadMore() {
        loading = true
        App.get().gitLab.getBranches(nextPageUrl.toString())
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    nextPageUrl = it.paginationData.next
                    adapterBranches.addEntries(it.body)
                }, {
                    Timber.e(it)
                    loading = false
                })
    }
}
