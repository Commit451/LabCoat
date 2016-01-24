package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.appthemeengine.ATE;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.ProjectNamespace;
import com.commit451.gitlab.util.AppThemeUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows a button to take you to a group
 */
public class ProjectMemberFooterViewHolder extends RecyclerView.ViewHolder{

    public static ProjectMemberFooterViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.footer_project_member, parent, false);
        return new ProjectMemberFooterViewHolder(view);
    }

    @Bind(R.id.button) Button mButton;

    public ProjectMemberFooterViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        ATE.apply(view, AppThemeUtil.resolveThemeKey(view.getContext()));
    }

    public void bind(ProjectNamespace namespace) {
        mButton.setText(String.format(itemView.getResources().getString(R.string.group_members), namespace.getName()));
    }
}
