package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.RepositoryTreeObject;
import com.commit451.gitlab.viewHolder.FileViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Shows the files
 */
public class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {

    private Listener listener;
    private ArrayList<RepositoryTreeObject> values;

    public FileAdapter(Listener listener) {
        this.listener = listener;
        values = new ArrayList<>();
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FileViewHolder holder = FileViewHolder.inflate(parent);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag(R.id.list_position);
                RepositoryTreeObject treeItem = getValueAt(position);

                if (treeItem.getType().equals(RepositoryTreeObject.TYPE_FOLDER)) {
                    listener.onFolderClicked(treeItem);
                } else if (treeItem.getType().equals(RepositoryTreeObject.TYPE_FILE)) {
                    listener.onFileClicked(treeItem);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final FileViewHolder holder, int position) {
        final RepositoryTreeObject treeItem = getValueAt(position);
        holder.bind(treeItem);
        holder.itemView.setTag(R.id.list_position, position);
        holder.popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_copy:
                        listener.onCopyClicked(treeItem);
                        return true;
                    case R.id.action_share:
                        listener.onShareClicked(treeItem);
                        return true;
                    case R.id.action_open:
                        listener.onOpenInBrowserClicked(treeItem);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public void setData(Collection<RepositoryTreeObject> values) {
        this.values.clear();
        if (values != null) {
            this.values.addAll(values);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        values.clear();
        notifyDataSetChanged();
    }

    public RepositoryTreeObject getValueAt(int position) {
        return values.get(position);
    }

    public interface Listener {
        void onFolderClicked(RepositoryTreeObject treeItem);
        void onFileClicked(RepositoryTreeObject treeItem);
        void onCopyClicked(RepositoryTreeObject treeItem);
        void onShareClicked(RepositoryTreeObject treeItem);
        void onOpenInBrowserClicked(RepositoryTreeObject treeItem);
    }
}
