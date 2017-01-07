package com.commit451.gitlab.viewHolder;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Shows assignee in a spinner
 */
public class AssigneeSpinnerViewHolder extends RecyclerView.ViewHolder {

    public static AssigneeSpinnerViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignee, parent, false);
        return new AssigneeSpinnerViewHolder(view);
    }

    @BindView(R.id.user_image)
    ImageView image;
    @BindView(R.id.user_username)
    TextView textUsername;

    public AssigneeSpinnerViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(@Nullable Member user) {
        if (user == null) {
            textUsername.setText(R.string.no_assignee);
            image.setImageResource(R.drawable.ic_assign_24dp);
        } else {
            textUsername.setText(user.getUsername());
            App.get().getPicasso()
                    .load(ImageUtil.getAvatarUrl(user, itemView.getResources().getDimensionPixelSize(R.dimen.user_list_image_size)))
                    .transform(new CircleTransformation())
                    .into(image);
        }
    }
}
