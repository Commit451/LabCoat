package com.commit451.gitlab.viewHolders;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.tools.DateUtils;
import com.commit451.gitlab.tools.ImageUtil;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.tkeunebr.gravatar.Gravatar;

/**
 * issues, yay!
 * Created by Jawn on 6/11/2015.
 */
public class IssueViewHolder extends RecyclerView.ViewHolder {

    public static IssueViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_issue, parent, false);
        return new IssueViewHolder(view);
    }

    @Bind(R.id.issue_image) ImageView image;
    @Bind(R.id.issue_message) TextView message;
    @Bind(R.id.issue_creator) TextView creator;

    public IssueViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Issue issue) {
        long tempId = issue.getIid();
        if(tempId < 1) {
            tempId = issue.getId();
        }

        message.setText("#" + tempId + ": " + issue.getTitle());

        CharSequence time = DateUtils.getRelativeTimeSpanString(itemView.getContext(), issue.getCreatedAt());
        creator.setText(String.format(itemView.getContext().getString(R.string.created_time), time, issue.getAuthor().getUsername()));

        Uri url = ImageUtil.getAvatarUrl(issue.getAssignee(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size));
        Picasso.with(itemView.getContext()).load(url).into(image);
    }
}
