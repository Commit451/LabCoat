package com.commit451.gitlab.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.PickBranchOrTagActivity
import com.commit451.gitlab.adapter.TagAdapter
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.Ref
import com.commit451.gitlab.model.api.Tag
import timber.log.Timber

/**
 * Pick a branch, any branch
 */
class PickTagFragment : ButterKnifeFragment() {

    companion object {

        private const val EXTRA_PROJECT_ID = "project_id"
        private const val EXTRA_CURRENT_REF = "current_ref"

        fun newInstance(projectId: Long, ref: Ref?): PickTagFragment {
            val fragment = PickTagFragment()
            val args = Bundle()
            args.putLong(EXTRA_PROJECT_ID, projectId)
            args.putParcelable(EXTRA_CURRENT_REF, ref)
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.list)
    lateinit var listProjects: RecyclerView
    @BindView(R.id.message_text)
    lateinit var textMessage: TextView
    @BindView(R.id.progress)
    lateinit var progress: View

    lateinit var adapterTags: TagAdapter

    var projectId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectId = arguments?.getLong(EXTRA_PROJECT_ID)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pick_tag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ref = arguments?.getParcelable<Ref>(EXTRA_CURRENT_REF)!!
        adapterTags = TagAdapter(ref, object : TagAdapter.Listener {
            override fun onTagClicked(entry: Tag) {
                val data = Intent()
                val newRef = Ref(Ref.TYPE_TAG, entry.name)
                data.putExtra(PickBranchOrTagActivity.EXTRA_REF, newRef)
                activity?.setResult(Activity.RESULT_OK, data)
                activity?.finish()
            }
        })
        listProjects.layoutManager = LinearLayoutManager(activity)
        listProjects.adapter = adapterTags

        loadData()
    }

    override fun loadData() {
        if (view == null) {
            return
        }
        progress.visibility = View.VISIBLE
        textMessage.visibility = View.GONE

        App.get().gitLab.getTags(projectId)
                .with(this)
                .subscribe({
                    progress.visibility = View.GONE
                    adapterTags.setEntries(it)
                }, {
                    Timber.e(it)
                    progress.visibility = View.GONE
                    textMessage.visibility = View.VISIBLE
                })
    }
}
