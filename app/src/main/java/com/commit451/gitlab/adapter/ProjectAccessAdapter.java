package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.viewHolders.ProjectAccessViewHolder;

/**
 * Created by Jawn on 9/16/2015.
 */
public class ProjectAccessAdapter extends RecyclerView.Adapter<ProjectAccessViewHolder> {

    public interface Listener {
        void onAccessLevelClicked(String accessLevel);
    }

    private Listener mListener;

    private String[] mValues;

    public ProjectAccessAdapter(Context context, Listener listener) {
        mListener = listener;
        mValues = context.getResources().getStringArray(R.array.role_names);
    }

    public String getValueAt(int position) {
        return mValues[position];
    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onAccessLevelClicked(getValueAt(position));
        }
    };

    @Override
    public ProjectAccessViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ProjectAccessViewHolder holder = ProjectAccessViewHolder.create(parent);
        holder.itemView.setOnClickListener(onProjectClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ProjectAccessViewHolder holder, int position) {
        String accessLevel = getValueAt(position);
        holder.bind(accessLevel);
        holder.itemView.setTag(R.id.list_position, position);
    }

    @Override
    public int getItemCount() {
        return mValues.length;
    }
}
