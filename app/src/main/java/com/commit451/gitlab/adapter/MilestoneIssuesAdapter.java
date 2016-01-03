package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.viewHolder.IssueViewHolder;
import com.commit451.gitlab.viewHolder.MilestoneHeaderViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Shows the issues associated with a {@link com.commit451.gitlab.model.api.Milestone}
 */
public class MilestoneIssuesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_MILESTONE = 1;

    private static final int HEADER_COUNT = 1;

    public interface Listener {
        void onIssueClicked(Issue issue);
    }
    private Listener mListener;
    private ArrayList<Issue> mValues;
    private Milestone mMilestone;

    public Issue getValueAt(int position) {
        return mValues.get(position - HEADER_COUNT);
    }

    public MilestoneIssuesAdapter(Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
    }

    private final View.OnClickListener mOnProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onIssueClicked(getValueAt(position));
        }
    };

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return MilestoneHeaderViewHolder.newInstance(parent);
            case TYPE_MILESTONE:
                IssueViewHolder issueViewHolder = IssueViewHolder.newInstance(parent);
                issueViewHolder.itemView.setOnClickListener(mOnProjectClickListener);
                return issueViewHolder;
        }
        throw new IllegalStateException("No holder for viewType " + viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MilestoneHeaderViewHolder) {
            ((MilestoneHeaderViewHolder) holder).bind(mMilestone);
        }
        if (holder instanceof IssueViewHolder) {
            Issue issue = getValueAt(position);
            ((IssueViewHolder) holder).bind(issue);
            holder.itemView.setTag(R.id.list_position, position);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size() + HEADER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_MILESTONE;
        }
    }

    public void setIssues(Collection<Issue> issues) {
        mValues.clear();
        if (issues != null) {
            mValues.addAll(issues);
        }
        notifyDataSetChanged();
    }

    public void setMilestone(Milestone milestone) {
        mMilestone = milestone;
        notifyItemChanged(0);
    }

    public void addIssue(Issue issue) {
        mValues.add(0, issue);
        notifyItemInserted(0);
    }

    public void updateIssue(Issue issue) {
        int indexToDelete = -1;
        for (int i=0; i<mValues.size(); i++) {
            if (mValues.get(i).getId() == issue.getId()) {
                indexToDelete = i;
                break;
            }
        }
        if (indexToDelete != -1) {
            mValues.remove(indexToDelete);
            mValues.add(indexToDelete, issue);
        }
        notifyItemChanged(indexToDelete);
    }
}
