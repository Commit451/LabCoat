package com.commit451.gitlab.viewHolder;

import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows an assignee
 * Created by Jawn on 12/18/2015.
 */
public class AssigneeViewHolder extends RecyclerView.ViewHolder {

    public static AssigneeViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignee, parent, false);
        return new AssigneeViewHolder(view);
    }

    @Bind(R.id.user_image) ImageView image;
    @Bind(R.id.user_username) TextView username;

    public AssigneeViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(@Nullable UserBasic user, int colorSelected, boolean isSelected) {
        if (user == null) {
            username.setText(R.string.no_assignee);
            image.setImageResource(R.drawable.ic_assign_24dp);
        } else {
            username.setText(user.getUsername());
            Uri url = ImageUtil.getAvatarUrl(user, itemView.getResources().getDimensionPixelSize(R.dimen.user_list_image_size));
            GitLabClient.getPicasso()
                    .load(url)
                    .into(image);
        }
        if (isSelected) {
            ((FrameLayout)itemView).setForeground(new ColorDrawable(colorSelected));
        } else {
            ((FrameLayout)itemView).setForeground(null);
        }
    }
}
