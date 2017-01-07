package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.RepositoryTreeObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Files, yay!
 */
public class FileViewHolder extends RecyclerView.ViewHolder {

    public static FileViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @BindView(R.id.file_title)
    TextView textTitle;
    @BindView(R.id.file_image)
    ImageView image;
    @BindView(R.id.file_more)
    ImageView buttonMore;

    public final PopupMenu popupMenu;

    public FileViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        popupMenu = new PopupMenu(itemView.getContext(), buttonMore);
        popupMenu.getMenuInflater().inflate(R.menu.item_menu_file, popupMenu.getMenu());

        buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    public void bind(RepositoryTreeObject treeItem) {
        textTitle.setText(treeItem.getName());
        image.setImageResource(treeItem.getDrawableForType());
    }
}
