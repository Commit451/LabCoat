package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Artifact;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Files, yay!
 */
public class BuildArtifactViewHolder extends RecyclerView.ViewHolder {

    public static BuildArtifactViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_build_artifact, parent, false);
        return new BuildArtifactViewHolder(view);
    }

    @BindView(R.id.file_title) TextView mTitleView;
    @BindView(R.id.file_image) ImageView mImageView;
    @BindView(R.id.file_more) ImageView mMoreView;

    public final PopupMenu popupMenu;

    public BuildArtifactViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        popupMenu = new PopupMenu(itemView.getContext(), mMoreView);
        popupMenu.getMenuInflater().inflate(R.menu.item_menu_file, popupMenu.getMenu());

        mMoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    public void bind(Artifact artifact) {
        mTitleView.setText(artifact.getName());
    }
}
