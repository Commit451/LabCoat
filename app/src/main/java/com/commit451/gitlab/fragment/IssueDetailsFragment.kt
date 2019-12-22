package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import coil.api.load
import coil.transform.CircleCropTransformation
import com.commit451.adapterflowlayout.AdapterFlowLayout
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.IssueLabelsAdapter
import com.commit451.gitlab.event.IssueChangedEvent
import com.commit451.gitlab.extension.setMarkdownText
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.util.DateUtil
import com.commit451.gitlab.util.ImageUtil
import com.commit451.gitlab.util.InternalLinkMovementMethod
import com.commit451.gitlab.viewHolder.IssueLabelViewHolder
import org.greenrobot.eventbus.Subscribe

/**
 * Shows the discussion of an issue
 */
class IssueDetailsFragment : ButterKnifeFragment() {

    companion object {

        private const val KEY_PROJECT = "project"
        private const val KEY_ISSUE = "issue"

        fun newInstance(project: Project, issue: Issue): IssueDetailsFragment {
            val fragment = IssueDetailsFragment()
            val args = Bundle()
            args.putParcelable(KEY_PROJECT, project)
            args.putParcelable(KEY_ISSUE, issue)
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.root)
    lateinit var root: ViewGroup
    @BindView(R.id.text_description)
    lateinit var textDescription: TextView
    @BindView(R.id.author_image)
    lateinit var imageAuthor: ImageView
    @BindView(R.id.author)
    lateinit var textAuthor: TextView
    @BindView(R.id.milestone_root)
    lateinit var rootMilestone: ViewGroup
    @BindView(R.id.milestone_text)
    lateinit var textMilestone: TextView
    @BindView(R.id.list_labels)
    lateinit var listLabels: AdapterFlowLayout

    lateinit var adapterLabels: IssueLabelsAdapter

    lateinit var project: Project
    lateinit var issue: Issue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments?.getParcelable(KEY_PROJECT)!!
        issue = arguments?.getParcelable(KEY_ISSUE)!!
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

        bind(issue, project)

        App.bus().register(this)
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    fun bind(issue: Issue, project: Project) {

        if (issue.description.isNullOrEmpty()) {
            textDescription.visibility = View.GONE
        } else {
            textDescription.visibility = View.VISIBLE
            textDescription.setMarkdownText(issue.description!!, project)
            textDescription.movementMethod = InternalLinkMovementMethod(App.get().getAccount().serverUrl!!)
        }

        imageAuthor.load(ImageUtil.getAvatarUrl(issue.author, resources.getDimensionPixelSize(R.dimen.image_size))) {
            transformations(CircleCropTransformation())
        }

        var author = ""
        if (issue.author != null) {
            author = issue.author!!.name + " "
        }
        author += resources.getString(R.string.created_issue)
        if (issue.createdAt != null) {
            author = author + " " + DateUtil.getRelativeTimeSpanString(baseActivty, issue.createdAt)
        }
        textAuthor.text = author
        if (issue.milestone != null) {
            rootMilestone.visibility = View.VISIBLE
            textMilestone.text = issue.milestone!!.title
        } else {
            rootMilestone.visibility = View.GONE
        }
        adapterLabels.setLabels(issue.labels)
    }

    @Subscribe
    fun onEvent(event: IssueChangedEvent) {
        if (issue.iid == event.issue.iid) {
            issue = event.issue
            bind(issue, project)
        }
    }
}
