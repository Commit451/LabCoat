package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.User;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.tkeunebr.gravatar.Gravatar;

/**
 * Users, yay!
 * Created by Jawn on 6/11/2015.
 */
public class UserViewHolder extends RecyclerView.ViewHolder {

    public static UserViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Bind(R.id.user_name) TextView name;
    @Bind(R.id.user_username) TextView username;
    @Bind(R.id.user_role) TextView role;
    @Bind(R.id.user_image) ImageView image;

    public UserViewHolder(View view) {
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

        int size = itemView.getResources().getDimensionPixelSize(R.dimen.image_size);

        String url = "http://www.gravatar.com/avatar/00000000000000000000000000000000?s=" + size;

        if(user.getEmail() != null) {
            url = Gravatar.init().with(user.getEmail()).size(size).build();
        }
        else if(user.getAvatarUrl() != null) {
            url = user.getAvatarUrl() + "&s=" + size;
        }

        Picasso.with(itemView.getContext())
                .load(url)
                .into(image);
    }
}
