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
public class ProjectsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int FOOTER_COUNT = 1;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    public interface Listener {
        void onProjectClicked(Project project);
    }

    private Listener mListener;
    private List<Project> mValues;
    private int[] mColors;
    private boolean mLoading;

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onProjectClicked(getValueAt(position));
        }
    };

    public ProjectsAdapter(Context context, Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
        mColors = context.getResources().getIntArray(R.array.cool_colors);
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
            ((ProjectViewHolder) holder).bind(project, mColors[position % mColors.length]);
            holder.itemView.setTag(R.id.list_position, position);
        } else if (holder instanceof LoadingFooterViewHolder) {
            ((LoadingFooterViewHolder) holder).bind(mLoading);
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

    public Project getValueAt(int position) {
        return mValues.get(position);
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

    public void setLoading(boolean loading) {
        mLoading = loading;
        notifyItemChanged(mValues.size());
    }
}
