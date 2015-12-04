package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Group;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * View associated with a group
 * Created by John on 10/8/15.
 */
public class GroupViewHolder extends RecyclerView.ViewHolder{

    public static GroupViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new GroupViewHolder(view);
    }

    @Bind(R.id.image) public ImageView image;
    @Bind(R.id.name) public TextView name;

    public GroupViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Group group) {
        name.setText(group.getName());
        GitLabClient.getPicasso()
                .load(group.getAvatarUrl())
                .into(image);
    }
}
