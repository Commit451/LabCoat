package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.adapterflowlayout.AdapterFlowLayout
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.IssueLabelsAdapter

/**
 * Shows the labels for an issue
 */
class IssueLabelsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): IssueLabelsViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.header_issue_labels, parent, false)
            return IssueLabelsViewHolder(view)
        }
    }

    @BindView(R.id.adapter_layout) lateinit var flowLayout: AdapterFlowLayout

    var adapterIssueLabels: IssueLabelsAdapter

    private val mListener = object : IssueLabelsAdapter.Listener {
        override fun onLabelClicked(label: String, viewHolder: IssueLabelViewHolder) {
            //TODO anything?
        }
    }

    init {
        ButterKnife.bind(this, view)
        adapterIssueLabels = IssueLabelsAdapter(mListener)
        flowLayout.adapter = adapterIssueLabels
    }

    fun bind(labels: Collection<String>) {
        adapterIssueLabels.setLabels(labels)
    }
}