package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.DiffActivity;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.DiffLine;
import com.commit451.gitlab.viewHolders.CommitViewHolder;

import java.util.List;

/**
 * Created by Jawn on 7/28/2015.
 */
public class CommitsAdapter extends RecyclerView.Adapter<CommitViewHolder> {

    private List<DiffLine> mValues;

    public DiffLine getValueAt(int position) {
        return mValues.get(position);
    }

    public CommitsAdapter(List<DiffLine> items) {
        mValues = items;
    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            v.getContext().startActivity(DiffActivity.newInstance(v.getContext(), getValueAt(position)));
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
        DiffLine commit = getValueAt(position);
        holder.bind(commit);
        holder.itemView.setTag(R.id.list_position, position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
