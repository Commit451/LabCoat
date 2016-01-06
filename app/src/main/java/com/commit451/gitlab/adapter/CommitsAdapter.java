package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.viewHolder.CommitViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Shows a list of commits to a project, seen in a project overview
 * Created by Jawn on 7/28/2015.
 */
public class CommitsAdapter extends RecyclerView.Adapter<CommitViewHolder> {

    public interface Listener {
        void onCommitClicked(RepositoryCommit commit);
    }
    private Listener mListener;
    private ArrayList<RepositoryCommit> mValues;

    public RepositoryCommit getValueAt(int position) {
        return mValues.get(position);
    }

    public CommitsAdapter(Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
    }

    public void addData(Collection<RepositoryCommit> commits) {
        if (commits != null) {
            mValues.addAll(commits);
            notifyItemRangeInserted(0, commits.size());
        }
        notifyDataSetChanged();
    }

    public void setData(Collection<RepositoryCommit> commits) {
        mValues.clear();
        addData(commits);
    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onCommitClicked(getValueAt(position));
        }
    };

    @Override
    public CommitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CommitViewHolder holder = CommitViewHolder.create(parent);
        holder.itemView.setOnClickListener(onProjectClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final CommitViewHolder holder, int position) {
        RepositoryCommit commit = getValueAt(position);
        holder.bind(commit);
        holder.itemView.setTag(R.id.list_position, position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
