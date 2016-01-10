package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.viewHolder.IssueViewHolder;
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Issues adapter
 * Created by Jawn on 7/28/2015.
 */
public class IssuesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int FOOTER_COUNT = 1;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    public interface Listener {
        void onIssueClicked(Issue issue);
    }
    private Listener mListener;
    private ArrayList<Issue> mValues;
    private boolean mLoading = false;

    public IssuesAdapter(Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onIssueClicked(getValueAt(position));
        }
    };

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                IssueViewHolder holder = IssueViewHolder.inflate(parent);
                holder.itemView.setOnClickListener(onProjectClickListener);
                return holder;
            case TYPE_FOOTER:
                return LoadingFooterViewHolder.inflate(parent);
        }
        throw new IllegalStateException("No holder for view type " + viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof IssueViewHolder) {
            Issue issue = getValueAt(position);
            ((IssueViewHolder) holder).bind(issue);
            holder.itemView.setTag(R.id.list_position, position);
        } else if (holder instanceof LoadingFooterViewHolder) {
            ((LoadingFooterViewHolder) holder).bind(mLoading);
        } else {
            throw new IllegalStateException("What is this holder?");
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size() + FOOTER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mValues.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    public void setIssues(Collection<Issue> issues) {
        mValues.clear();
        addIssues(issues);
    }

    public void addIssues(Collection<Issue> issues) {
        if (issues != null) {
            mValues.addAll(issues);
        }
        notifyDataSetChanged();
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

    public Issue getValueAt(int position) {
        return mValues.get(position);
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
        notifyItemChanged(mValues.size());
    }
}
