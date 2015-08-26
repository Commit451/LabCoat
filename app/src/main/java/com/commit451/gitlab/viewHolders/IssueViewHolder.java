package com.commit451.gitlab.viewHolders;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
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
    @Bind(R.id.issue_state) TextView stateView;

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

        String state = issue.getState();
        stateView.setText(state);
        if(state != null && (state.equals("opened") || state.equals("reopened"))) {
            stateView.setTextColor(Color.parseColor("#30C830"));
        }
        else if(state != null && (state.equals("closed"))) {
            stateView.setTextColor(Color.parseColor("#FF0000"));
        }

        int size = itemView.getResources().getDimensionPixelSize(R.dimen.image_size);

        String assigneeName = "Unassigned";
        String assigneeAvatarUrl = "http://www.gravatar.com/avatar/00000000000000000000000000000000?s=" + size;

        if(issue.getAssignee() != null) {
            assigneeName = issue.getAssignee().getName();

            if(issue.getAssignee().getEmail() != null)
                assigneeAvatarUrl = Gravatar.init()
                        .with(issue.getAssignee().getEmail())
                        .size(itemView.getResources().getDimensionPixelSize(R.dimen.image_size))
                        .build();
            else if(issue.getAssignee().getAvatarUrl() != null)
                assigneeAvatarUrl = issue.getAssignee().getAvatarUrl() + "&s=" + size;
        }

        creator.setText(assigneeName);
        Picasso.with(itemView.getContext()).load(assigneeAvatarUrl).into(image);
    }
}
