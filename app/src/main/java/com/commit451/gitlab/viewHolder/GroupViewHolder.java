package com.commit451.gitlab.viewHolder;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Group;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * View associated with a group
 */
public class GroupViewHolder extends RecyclerView.ViewHolder {

    public static GroupViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new GroupViewHolder(view);
    }

    @BindView(R.id.image)
    public ImageView image;
    @BindView(R.id.name)
    public TextView textName;

    public GroupViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Group group) {
        textName.setText(group.getName());

        if (group.getAvatarUrl() != null && !group.getAvatarUrl().equals(Uri.EMPTY)) {
            App.get().getPicasso()
                    .load(group.getAvatarUrl())
                    .into(image);
        }
    }
}
