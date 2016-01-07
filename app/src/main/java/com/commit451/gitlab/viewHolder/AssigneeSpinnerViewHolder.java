package com.commit451.gitlab.viewHolder;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows assignee in a spinner
 */
public class AssigneeSpinnerViewHolder extends RecyclerView.ViewHolder {

    public static AssigneeSpinnerViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignee, parent, false);
        return new AssigneeSpinnerViewHolder(view);
    }

    @Bind(R.id.user_image)
    ImageView mImageView;
    @Bind(R.id.user_username)
    TextView mUsernameView;

    public AssigneeSpinnerViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(@Nullable Member user) {
        if (user == null) {
            mUsernameView.setText(R.string.no_assignee);
            mImageView.setImageResource(R.drawable.ic_assign_24dp);
        } else {
            mUsernameView.setText(user.getUsername());
            GitLabClient.getPicasso()
                    .load(ImageUtil.getAvatarUrl(user, itemView.getResources().getDimensionPixelSize(R.dimen.user_list_image_size)))
                    .into(mImageView);
        }
    }
}
