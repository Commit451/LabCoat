package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder;
import com.commit451.gitlab.viewHolder.ProjectViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Shows a list of projects
 */
public class ProjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int FOOTER_COUNT = 1;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private Listener listener;
    private List<Project> values;
    private int[] colors;
    private boolean loading;

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            listener.onProjectClicked(getValueAt(position));
        }
    };

    public ProjectAdapter(Context context, Listener listener) {
        this.listener = listener;
        values = new ArrayList<>();
        colors = context.getResources().getIntArray(R.array.cool_colors);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                ProjectViewHolder holder = ProjectViewHolder.inflate(parent);
                holder.itemView.setOnClickListener(onProjectClickListener);
                return holder;
            case TYPE_FOOTER:
                return LoadingFooterViewHolder.inflate(parent);
        }
        throw new IllegalStateException("No idea what to create for view type " + viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProjectViewHolder) {
            Project project = getValueAt(position);
            ((ProjectViewHolder) holder).bind(project, colors[position % colors.length]);
            holder.itemView.setTag(R.id.list_position, position);
        } else if (holder instanceof LoadingFooterViewHolder) {
            ((LoadingFooterViewHolder) holder).bind(loading);
        }
    }

    @Override
    public int getItemCount() {
        return values.size() + FOOTER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == values.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    public Project getValueAt(int position) {
        return values.get(position);
    }

    public void clearData() {
        values.clear();
        notifyDataSetChanged();
    }

    public void setData(Collection<Project> projects) {
        values.clear();
        if (projects != null) {
            values.addAll(projects);
        }
        notifyDataSetChanged();
    }

    public void addData(Collection<Project> projects) {
        values.addAll(projects);
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyItemChanged(values.size());
    }

    public interface Listener {
        void onProjectClicked(Project project);
    }
}
