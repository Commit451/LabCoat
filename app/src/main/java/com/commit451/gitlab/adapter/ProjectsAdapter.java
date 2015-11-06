package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.viewHolders.ProjectViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Jawn on 7/28/2015.
 */
public class ProjectsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onProjectClicked(Project project);
    }

    private Listener mListener;
    private List<Project> mValues;
    private int[] mColors;

    public Project getValueAt(int position) {
        return mValues.get(position);
    }

    public ProjectsAdapter(Context context, Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
        mColors = context.getResources().getIntArray(R.array.cool_colors);
    }

    public void clearData() {
        mValues.clear();
        notifyDataSetChanged();
    }

    public void setData(Collection<Project> projects) {
        mValues.clear();
        if (projects != null) {
            mValues.addAll(projects);
        }
        notifyDataSetChanged();
    }

    public void addData(Collection<Project> projects) {
        mValues.addAll(projects);
        notifyDataSetChanged();
    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onProjectClicked(getValueAt(position));
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
            ((ProjectViewHolder) holder).bind(project, mColors[position % mColors.length]);
            holder.itemView.setTag(R.id.list_position, position);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
