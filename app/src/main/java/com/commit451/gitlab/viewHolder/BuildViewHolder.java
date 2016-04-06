package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.util.DateUtils;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Builds, woot
 */
public class BuildViewHolder extends RecyclerView.ViewHolder {

    public static BuildViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_build, parent, false);
        return new BuildViewHolder(view);
    }

    @Bind(R.id.number) TextView buildNumber;
    @Bind(R.id.status) TextView status;
    @Bind(R.id.duration) TextView duration;

    public BuildViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Build build) {
        buildNumber.setText(itemView.getResources().getString(R.string.build_number) + build.getId());
        String statusText = String.format(itemView.getResources().getString(R.string.build_status), build.getStatus());
        status.setText(statusText);
        Date finishedTime = build.getFinishedAt();
        if (finishedTime == null) {
            finishedTime = new Date();
        }
        String timeTaken = DateUtils.getTimeTaken(build.getStartedAt(), finishedTime);
        String durationStr = String.format(itemView.getResources().getString(R.string.build_duration), timeTaken);
        duration.setText(durationStr);
    }
}
