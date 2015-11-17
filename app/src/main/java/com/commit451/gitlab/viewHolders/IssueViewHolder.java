package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Issue;
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

        int size = itemView.getResources().getDimensionPixelSize(R.dimen.image_size);

        //TODO why is this hard coded? Urg
        String assigneeName = "Unassigned";
        String assigneeAvatarUrl = "http://www.gravatar.com/avatar/00000000000000000000000000000000?s=" + size;

        if(issue.getAssignee() != null) {
            assigneeName = issue.getAssignee().getName();

            if(issue.getAssignee().getEmail() != null) {
                assigneeAvatarUrl = Gravatar.init()
                        .with(issue.getAssignee().getEmail())
                        .size(itemView.getResources().getDimensionPixelSize(R.dimen.image_size))
                        .build();
            }
            else if(issue.getAssignee().getAvatarUrl() != null) {
                assigneeAvatarUrl = issue.getAssignee().getAvatarUrl() + "&s=" + size;
            }
        }

        CharSequence time = DateUtils.getRelativeTimeSpanString(issue.getCreatedAt().getTime());
        creator.setText(String.format(itemView.getContext().getString(R.string.created_time), time, issue.getAuthor().getUsername()));
        Picasso.with(itemView.getContext()).load(assigneeAvatarUrl).into(image);
    }
}
