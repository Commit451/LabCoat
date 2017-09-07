package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import com.commit451.adapterflowlayout.AdapterFlowLayout
import com.commit451.addendum.parceler.getParcelerParcelable
import com.commit451.addendum.parceler.putParcelerParcelable
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.IssueLabelsAdapter
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.extension.setMarkdownText
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.transformation.CircleTransformation
import com.commit451.gitlab.util.DateUtil
import com.commit451.gitlab.util.ImageUtil
import com.commit451.gitlab.util.InternalLinkMovementMethod
import com.commit451.gitlab.viewHolder.IssueLabelViewHolder
import org.greenrobot.eventbus.Subscribe

/**
 * Shows the discussion of an issue
 */
class MergeRequestDetailsFragment : ButterKnifeFragment() {

    companion object {

        private val KEY_PROJECT = "project"
        private val KEY_MERGE_REQUEST = "merge_request"

        fun newInstance(project: Project, mergeRequest: MergeRequest): MergeRequestDetailsFragment {
            val fragment = MergeRequestDetailsFragment()
            val args = Bundle()
            args.putParcelerParcelable(KEY_PROJECT, project)
            args.putParcelerParcelable(KEY_MERGE_REQUEST, mergeRequest)
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.root) lateinit var root: ViewGroup
    @BindView(R.id.text_description) lateinit var textDescription: TextView
    @BindView(R.id.author_image) lateinit var imageAuthor: ImageView
    @BindView(R.id.author) lateinit var textAuthor: TextView
    @BindView(R.id.milestone_root) lateinit var rootMilestone: ViewGroup
    @BindView(R.id.milestone_text) lateinit var textMilestone: TextView
    @BindView(R.id.list_labels) lateinit var listLabels: AdapterFlowLayout

    lateinit var adapterLabels: IssueLabelsAdapter

    lateinit var project: Project
    lateinit var mergeRequest: MergeRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments.getParcelerParcelable<Project>(KEY_PROJECT)!!
        mergeRequest = arguments.getParcelerParcelable<MergeRequest>(KEY_MERGE_REQUEST)!!
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_issue_details, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
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

        App.get().picasso
                .load(ImageUtil.getAvatarUrl(mergeRequest.author, resources.getDimensionPixelSize(R.dimen.image_size)))
                .transform(CircleTransformation())
                .into(imageAuthor)

        var author = ""
        if (mergeRequest.author != null) {
            author = mergeRequest.author!!.name + " "
        }
        author += resources.getString(R.string.created_issue)
        if (mergeRequest.createdAt != null) {
            author = author + " " + DateUtil.getRelativeTimeSpanString(context, mergeRequest.createdAt)
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