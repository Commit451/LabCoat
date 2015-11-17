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

/**
 * Shows a single user
 * Created by John on 9/28/15.
 */
public class UserViewHolder extends RecyclerView.ViewHolder {

    public static UserViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Bind(R.id.user_name) public TextView name;
    @Bind(R.id.user_username) public TextView username;
    @Bind(R.id.user_image) public ImageView image;

    public UserViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(User user) {
        name.setText(user.getName());
        if(user.getUsername() != null) {
            username.setText(user.getUsername());
        }

        int size = itemView.getResources().getDimensionPixelSize(R.dimen.image_size);
        String url = ImageUtil.getGravatarUrl(user, size);

        Picasso.with(itemView.getContext())
                .load(url)
                .into(image);
    }
}
