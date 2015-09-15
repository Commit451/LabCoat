package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.events.CloseDrawerEvent;
import com.commit451.gitlab.events.ProjectChangedEvent;
import com.commit451.gitlab.model.NavItem;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.tools.Prefs;
import com.commit451.gitlab.tools.Repository;
import com.commit451.gitlab.viewHolders.ProjectViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jawn on 7/28/2015.
 */
public class ProjectsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Project> mValues;
    private List<NavItem> mNavItems;

    public Project getValueAt(int position) {
        return mValues.get(position);
    }

    public ProjectsAdapter(List<Project> items) {
        mValues = items;
        mNavItems = new ArrayList<>();

    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            if(GitLabApp.instance().getSelectedProject() == null || !GitLabApp.instance().getSelectedProject().equals(Repository.projects.get(position))) {
                //TODO make the event bus control most of this. NO MORE STATIC UI
                GitLabApp.instance().setSelectedProject(Repository.projects.get(position));
                Prefs.setLastProject(v.getContext(), GitLabApp.instance().getSelectedProject().toString());
                notifyDataSetChanged();
            }
            GitLabApp.bus().post(new CloseDrawerEvent());
            GitLabApp.bus().post(new ProjectChangedEvent(position));
        }
    };

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ProjectViewHolder holder = ProjectViewHolder.create(parent);
        holder.itemView.setOnClickListener(onProjectClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProjectViewHolder) {
            Project project = getValueAt(position);
            ((ProjectViewHolder) holder).bind(project);
            holder.itemView.setTag(R.id.list_position, position);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
