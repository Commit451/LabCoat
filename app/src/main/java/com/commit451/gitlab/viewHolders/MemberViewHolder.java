package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.ImageUtil;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.tkeunebr.gravatar.Gravatar;

/**
 * Users, yay!
 * Created by Jawn on 6/11/2015.
 */
public class MemberViewHolder extends RecyclerView.ViewHolder {

    public static MemberViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Bind(R.id.user_name) public TextView name;
    @Bind(R.id.user_username) public TextView username;
    @Bind(R.id.user_role) public TextView role;
    @Bind(R.id.user_image) public ImageView image;

    public MemberViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(User user) {
        name.setText(user.getName());
        if(user.getUsername() != null) {
            username.setText(user.getUsername());
        }

        if (user.getAccessLevel() != -1) {
            role.setVisibility(View.VISIBLE);
            role.setText(user.getAccessLevel(itemView.getResources().getStringArray(R.array.role_names)));
        } else {
            role.setVisibility(View.GONE);
        }

        String url = ImageUtil.getAvatarUrl(user, itemView.getResources().getDimensionPixelSize(R.dimen.image_size));
        Picasso.with(itemView.getContext())
                .load(url)
                .into(image);
    }
}
