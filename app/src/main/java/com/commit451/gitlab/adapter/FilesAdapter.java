package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.TreeItem;
import com.commit451.gitlab.viewHolder.FileViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Shows the files
 * Created by Jawnnypoo on 11/22/2015.
 */
public class FilesAdapter extends RecyclerView.Adapter<FileViewHolder> {

    public interface Listener {
        void onFolderClicked(TreeItem treeItem);
        void onFileClicked(TreeItem treeItem);
        void onCopyClicked(TreeItem treeItem);
        void onShareClicked(TreeItem treeItem);
        void onOpenInBrowserClicked(TreeItem treeItem);
    }
    private Listener mListener;
    private ArrayList<TreeItem> mValues;

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            TreeItem treeItem = getValueAt(position);

            if(treeItem.getType().equals(TreeItem.TYPE_FOLDER)) {
                mListener.onFolderClicked(treeItem);
            }
            else if(treeItem.getType().equals(TreeItem.TYPE_FILE)) {
                mListener.onFileClicked(treeItem);
            }
        }
    };

    public FilesAdapter(Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FileViewHolder holder = FileViewHolder.newInstance(parent);
        holder.itemView.setOnClickListener(onProjectClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final FileViewHolder holder, int position) {
        final TreeItem treeItem = getValueAt(position);
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

    public void setData(Collection<TreeItem> values) {
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

    public TreeItem getValueAt(int position) {
        return mValues.get(position);
    }
}
