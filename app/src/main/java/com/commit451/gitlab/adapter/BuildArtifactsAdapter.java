package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Artifact;
import com.commit451.gitlab.viewHolder.BuildArtifactViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Show the build artifacts
 */
public class BuildArtifactsAdapter extends RecyclerView.Adapter<BuildArtifactViewHolder> {

    public interface Listener {
        void onFolderClicked(Artifact treeItem);
        void onFileClicked(Artifact treeItem);
        void onCopyClicked(Artifact treeItem);
        void onShareClicked(Artifact treeItem);
        void onOpenInBrowserClicked(Artifact treeItem);
    }
    private Listener mListener;
    private ArrayList<Artifact> mValues;

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            Artifact treeItem = getValueAt(position);
        }
    };

    public BuildArtifactsAdapter(Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
    }

    @Override
    public BuildArtifactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BuildArtifactViewHolder holder = BuildArtifactViewHolder.inflate(parent);
        holder.itemView.setOnClickListener(onProjectClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final BuildArtifactViewHolder holder, int position) {
        final Artifact treeItem = getValueAt(position);
        holder.bind(treeItem);
        holder.itemView.setTag(R.id.list_position, position);
        holder.popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_copy:
                        mListener.onCopyClicked(treeItem);
                        return true;
                    case R.id.action_share:
                        mListener.onShareClicked(treeItem);
                        return true;
                    case R.id.action_open:
                        mListener.onOpenInBrowserClicked(treeItem);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setData(Collection<Artifact> values) {
        mValues.clear();
        if (values != null) {
            mValues.addAll(values);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

    public Artifact getValueAt(int position) {
        return mValues.get(position);
    }
}
