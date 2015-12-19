package com.commit451.gitlab.viewHolders;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Users, yay!
 * Created by Jawn on 6/11/2015.
 */
public class MemberGroupViewHolder extends RecyclerView.ViewHolder {

    public static MemberGroupViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_group, parent, false);
        return new MemberGroupViewHolder(view);
    }

    @Bind(R.id.user_name) public TextView name;
    @Bind(R.id.user_username) public TextView username;
    @Bind(R.id.user_image) public ImageView image;

    public MemberGroupViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(User user) {
        name.setText(user.getName());
        if(user.getUsername() != null) {
            username.setText(user.getUsername());
        }

        Uri url = ImageUtil.getAvatarUrl(user, itemView.getResources().getDimensionPixelSize(R.dimen.image_size));
        GitLabClient.getPicasso()
                .load(url)
                .into(image);
    }
}
