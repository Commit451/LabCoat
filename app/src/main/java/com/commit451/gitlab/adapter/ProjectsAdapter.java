package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.events.CloseDrawerEvent;
import com.commit451.gitlab.events.ProjectChangedEvent;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.tools.Prefs;
import com.commit451.gitlab.tools.Repository;
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
            if(Repository.selectedProject == null || !Repository.selectedProject.equals(Repository.projects.get(position))) {
                //TODO make the event bus control most of this. NO MORE STATIC UI
                Repository.selectedProject = Repository.projects.get(position);
                Prefs.setLastProject(v.getContext(), Repository.selectedProject.toString());
                Repository.issueAdapter = null;
                Repository.userAdapter = null;
                notifyDataSetChanged();
            }
            GitLabApp.bus().post(new CloseDrawerEvent());
            GitLabApp.bus().post(new ProjectChangedEvent(position));
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
