package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.api.load
import coil.transform.CircleCropTransformation
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.IssueLabelsAdapter
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.extension.setMarkdownText
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.util.DateUtil
import com.commit451.gitlab.util.ImageUtil
import com.commit451.gitlab.util.InternalLinkMovementMethod
import com.commit451.gitlab.viewHolder.IssueLabelViewHolder
import kotlinx.android.synthetic.main.fragment_issue_details.*
import org.greenrobot.eventbus.Subscribe

/**
 * Shows the discussion of an issue
 */
class MergeRequestDetailsFragment : BaseFragment() {

    companion object {

        private const val KEY_PROJECT = "project"
        private const val KEY_MERGE_REQUEST = "merge_request"

        fun newInstance(project: Project, mergeRequest: MergeRequest): MergeRequestDetailsFragment {
            val fragment = MergeRequestDetailsFragment()
            val args = Bundle()
            args.putParcelable(KEY_PROJECT, project)
            args.putParcelable(KEY_MERGE_REQUEST, mergeRequest)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var adapterLabels: IssueLabelsAdapter

    private lateinit var project: Project
    private lateinit var mergeRequest: MergeRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments?.getParcelable(KEY_PROJECT)!!
        mergeRequest = arguments?.getParcelable(KEY_MERGE_REQUEST)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_issue_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterLabels = IssueLabelsAdapter(object : IssueLabelsAdapter.Listener {
            override fun onLabelClicked(label: String, viewHolder: IssueLabelViewHolder) {

            }
        })
        listLabels.adapter = adapterLabels

        bind(mergeRequest, project)

        App.bus().register(this)
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    fun bind(mergeRequest: MergeRequest, project: Project) {

        if (mergeRequest.description.isNullOrEmpty()) {
            textDescription.visibility = View.GONE
        } else {
            textDescription.visibility = View.VISIBLE
            textDescription.setMarkdownText(mergeRequest.description!!, project)
            textDescription.movementMethod = InternalLinkMovementMethod(App.get().getAccount().serverUrl!!)
        }

        imageAuthor.load(ImageUtil.getAvatarUrl(mergeRequest.author, resources.getDimensionPixelSize(R.dimen.image_size))) {
            transformations(CircleCropTransformation())
        }

        var author = ""
        if (mergeRequest.author != null) {
            author = mergeRequest.author!!.name + " "
        }
        author += resources.getString(R.string.created_issue)
        if (mergeRequest.createdAt != null) {
            author = author + " " + DateUtil.getRelativeTimeSpanString(baseActivty, mergeRequest.createdAt)
        }
        textAuthor.text = author
        if (mergeRequest.milestone != null) {
            rootMilestone.visibility = View.VISIBLE
            textMilestone.text = mergeRequest.milestone!!.title
        } else {
            rootMilestone.visibility = View.GONE
        }
        adapterLabels.setLabels(mergeRequest.labels)
    }

    @Subscribe
    fun onEvent(event: MergeRequestChangedEvent) {
        if (mergeRequest.iid == event.mergeRequest.iid) {
            mergeRequest = event.mergeRequest
            bind(mergeRequest, project)
        }
    }
}
