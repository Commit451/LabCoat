package com.commit451.gitlab.viewHolder;

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
import com.commit451.gitlab.util.ImageUtil;

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

    @Bind(R.id.name) public TextView username;
    @Bind(R.id.image) public ImageView image;

    public MemberGroupViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(User user) {
        username.setText(user.getUsername());

        Uri url = ImageUtil.getAvatarUrl(user, itemView.getResources().getDimensionPixelSize(R.dimen.user_header_image_size));
        GitLabClient.getPicasso()
                .load(url)
                .into(image);
    }
}
