package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.TreeItem;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Files, yay!
 * Created by Jawn on 6/11/2015.
 */
public class FileViewHolder extends RecyclerView.ViewHolder {

    public static FileViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Bind(R.id.file_title) TextView title;
    @Bind(R.id.file_image) ImageView image;
    @Bind(R.id.file_more) ImageView more;

    public FileViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(TreeItem treeItem) {
        title.setText(treeItem.getName());
        if(treeItem.getType().equals("tree")) {
            image.setImageResource(R.drawable.ic_folder_24dp);
        }
        else if(treeItem.getType().equals("submodule")) {
            image.setImageResource(R.drawable.ic_repo_24dp);
        }
        else {
            image.setImageResource(R.drawable.ic_file_24dp);
        }
    }
}
