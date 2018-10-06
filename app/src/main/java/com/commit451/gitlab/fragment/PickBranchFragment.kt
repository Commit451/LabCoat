package com.commit451.gitlab.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.commit451.addendum.parceler.getParcelerParcelable
import com.commit451.addendum.parceler.putParcelerParcelable
import com.commit451.addendum.parceler.putParcelerParcelableExtra
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.PickBranchOrTagActivity
import com.commit451.gitlab.adapter.BranchAdapter
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.Ref
import com.commit451.gitlab.model.api.Branch
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import com.commit451.gitlab.util.OnScrollLoadMoreListener
import timber.log.Timber

/**
 * Pick a branch, any branch
 */
class PickBranchFragment : ButterKnifeFragment() {

    companion object {

        private const val EXTRA_PROJECT_ID = "project_id"
        private const val EXTRA_REF = "ref"

        fun newInstance(projectId: Long, ref: Ref?): PickBranchFragment {
            val fragment = PickBranchFragment()
            val args = Bundle()
            args.putLong(EXTRA_PROJECT_ID, projectId)
            args.putParcelerParcelable(EXTRA_REF, ref)
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.list)
    lateinit var listProjects: androidx.recyclerview.widget.RecyclerView
    @BindView(R.id.message_text)
    lateinit var textMessage: TextView
    @BindView(R.id.progress)
    lateinit var progress: View

    lateinit var adapterBranches: BranchAdapter

    var projectId: Long = 0

    var nextPageUrl: Uri? = null
    var loading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectId = arguments?.getLong(EXTRA_PROJECT_ID)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pick_branch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val existingRef = arguments?.getParcelerParcelable<Ref>(EXTRA_REF)!!
        adapterBranches = BranchAdapter(existingRef, object : BranchAdapter.Listener {
            override fun onBranchClicked(entry: Branch) {
                val data = Intent()
                val ref = Ref(Ref.TYPE_BRANCH, entry.name)
                data.putParcelerParcelableExtra(PickBranchOrTagActivity.EXTRA_REF, ref)
                activity?.setResult(Activity.RESULT_OK, data)
                activity?.finish()
            }
        })
        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
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
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Branch>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        progress.visibility = View.GONE
                        textMessage.visibility = View.VISIBLE
                    }

                    override fun responseNonNullSuccess(branches: List<Branch>) {
                        loading = false
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        progress.visibility = View.GONE
                        adapterBranches.setEntries(branches)
                    }
                })
    }

    fun loadMore() {
        loading = true
        App.get().gitLab.getBranches(nextPageUrl.toString())
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Branch>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        loading = false
                    }

                    override fun responseNonNullSuccess(branches: List<Branch>) {
                        loading = false
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapterBranches.addEntries(branches)
                    }
                })
    }
}
