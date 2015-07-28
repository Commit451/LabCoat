package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.viewHolders.ProjectViewHolder;

import java.util.List;

/**
 * Created by Jawn on 7/28/2015.
 */
public class ProjectsAdapter extends RecyclerView.Adapter<ProjectViewHolder> {

    private List<Project> mValues;

    public Project getValueAt(int position) {
        return mValues.get(position);
    }

    public ProjectsAdapter(List<Project> items) {
        mValues = items;
    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);

            //do the things

        }
    };

    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ProjectViewHolder holder = ProjectViewHolder.create(parent);
        holder.itemView.setOnClickListener(onProjectClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ProjectViewHolder holder, int position) {
        Project project = getValueAt(position);
        holder.bind(project);
        holder.itemView.setTag(R.id.list_position, position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
