package com.commit451.gitlab.viewHolders;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.tools.Repository;
import com.commit451.gitlab.views.CompoundTextView;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.tkeunebr.gravatar.Gravatar;

/**
 * Files, yay!
 * Created by Jawn on 6/11/2015.
 */
public class IssueViewHolder extends RecyclerView.ViewHolder {

    public static IssueViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_issue, parent, false);
        return new IssueViewHolder(view);
    }

    @Bind(R.id.title) TextView title;
    @Bind(R.id.summary) CompoundTextView summary;
    @Bind(R.id.custom) TextView custom;

    public IssueViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Issue issue) {
        long tempId = issue.getIid();
        if(tempId < 1) {
            tempId = issue.getId();
        }

        title.setText("#" + tempId + ": " + issue.getTitle());

        String state = issue.getState();
        custom.setText(state);
        if(state != null && (state.equals("opened") || state.equals("reopened"))) {
            custom.setTextColor(Color.parseColor("#30C830"));
        }
        else if(state != null && (state.equals("closed"))) {
            custom.setTextColor(Color.parseColor("#FF0000"));
        }

        float percent = Repository.displayWidth / 720f;
        int size = (int) (40f * percent);

        String assigneeName = "Unassigned";
        String assigneeAvatarUrl = "http://www.gravatar.com/avatar/00000000000000000000000000000000?s=" + size;

        if(issue.getAssignee() != null) {
            assigneeName = issue.getAssignee().getName();

            if(issue.getAssignee().getEmail() != null)
                assigneeAvatarUrl = Gravatar.init().with(issue.getAssignee().getEmail()).size(size).build();
            else if(issue.getAssignee().getAvatarUrl() != null)
                assigneeAvatarUrl = issue.getAssignee().getAvatarUrl() + "&s=" + size;
        }

        summary.setText(assigneeName);
        Picasso.with(itemView.getContext()).load(assigneeAvatarUrl).into(summary);
    }
}
