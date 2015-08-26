package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.activities.IssueActivity;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.viewHolders.IssueViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Jawn on 7/28/2015.
 */
public class IssuesAdapter extends RecyclerView.Adapter<IssueViewHolder> {

    private ArrayList<Issue> mValues;

    public Issue getValueAt(int position) {
        return mValues.get(position);
    }

    public IssuesAdapter() {
        mValues = new ArrayList<>();
    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            v.getContext().startActivity(IssueActivity.newInstance(v.getContext(), getValueAt(position)));
        }
    };

    @Override
    public IssueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        IssueViewHolder holder = IssueViewHolder.create(parent);
        holder.itemView.setOnClickListener(onProjectClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final IssueViewHolder holder, int position) {
        Issue issue = getValueAt(position);
        holder.bind(issue);
        holder.itemView.setTag(R.id.list_position, position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setIssues(Collection<Issue> issues) {
        mValues.clear();
        if (issues != null) {
            mValues.addAll(issues);
        }
        notifyDataSetChanged();
    }

    public void addIssue(Issue issue) {
        mValues.add(0, issue);
        notifyItemInserted(0);
    }
}
