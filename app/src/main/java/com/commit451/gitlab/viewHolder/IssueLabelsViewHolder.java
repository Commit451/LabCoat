package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.IssueLabelsAdapter;
import com.commit451.gitlab.view.AdapterFlowLayout;

import java.util.Collection;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows the labels for an issue
 */
public class IssueLabelsViewHolder extends RecyclerView.ViewHolder {

    public static IssueLabelsViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_issue_labels, parent, false);
        return new IssueLabelsViewHolder(view);
    }

    @Bind(R.id.adapter_layout)
    AdapterFlowLayout mFlowLayout;
    IssueLabelsAdapter mAdapter;

    private final IssueLabelsAdapter.Listener mListener = new IssueLabelsAdapter.Listener() {
        @Override
        public void onLabelClicked(String label, LabelViewHolder viewHolder) {

        }
    };

    public IssueLabelsViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        mAdapter = new IssueLabelsAdapter(mListener);
        mFlowLayout.setAdapter(mAdapter);
    }

    public void bind(Collection<String> labels) {
        mAdapter.setLabels(labels);
    }
}