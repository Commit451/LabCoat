package com.commit451.gitlab.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
import com.commit451.gitlab.rx.CustomSingleObserver
import timber.log.Timber

/**
 * Pick a branch, any branch
 */
class PickBranchFragment : ButterKnifeFragment() {

    companion object {

        private val EXTRA_PROJECT_ID = "project_id"
        private val EXTRA_REF = "ref"

        fun newInstance(projectId: Long, ref: Ref?): PickBranchFragment {
            val fragment = PickBranchFragment()
            val args = Bundle()
            args.putLong(EXTRA_PROJECT_ID, projectId)
            args.putParcelerParcelable(EXTRA_REF, ref)
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.list) lateinit var listProjects: RecyclerView
    @BindView(R.id.message_text) lateinit var textMessage: TextView
    @BindView(R.id.progress) lateinit var progress: View

    lateinit var adapterBranches: BranchAdapter

    var projectId: Long = 0

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
        listProjects.layoutManager = LinearLayoutManager(activity)
        listProjects.adapter = adapterBranches

        loadData()
    }

    override fun loadData() {
        if (view == null) {
            return
        }
        progress.visibility = View.VISIBLE
        textMessage.visibility = View.GONE

        App.get().gitLab.getBranches(projectId)
                .with(this)
                .subscribe(object : CustomSingleObserver<List<Branch>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        progress.visibility = View.GONE
                        textMessage.visibility = View.VISIBLE
                    }

                    override fun success(branches: List<Branch>) {
                        progress.visibility = View.GONE
                        adapterBranches.setEntries(branches)
                    }
                })
    }
}
