package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
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

    public static FileViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Bind(R.id.file_title) public TextView title;
    @Bind(R.id.file_image) public ImageView image;
    @Bind(R.id.file_more) public ImageView more;
    public PopupMenu popupMenu;

    private final View.OnClickListener mOnMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            popupMenu.show();
        }
    };

    public FileViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        popupMenu = new PopupMenu(itemView.getContext(), more);
        popupMenu.getMenuInflater().inflate(R.menu.menu_file, popupMenu.getMenu());
        more.setOnClickListener(mOnMoreClickListener);
    }

    public void bind(TreeItem treeItem) {
        title.setText(treeItem.getName());
        image.setImageResource(treeItem.getDrawableForType());
    }
}
