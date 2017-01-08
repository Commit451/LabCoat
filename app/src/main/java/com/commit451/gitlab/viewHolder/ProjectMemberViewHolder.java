package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Shows a project member
 */
public class ProjectMemberViewHolder extends RecyclerView.ViewHolder {

    public static ProjectMemberViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_project, parent, false);
        return new ProjectMemberViewHolder(view);
    }

    @BindView(R.id.overflow)
    View buttonOverflow;
    @BindView(R.id.name)
    TextView textUsername;
    @BindView(R.id.access)
    TextView textAccess;
    @BindView(R.id.image)
    public ImageView image;

    public final PopupMenu popupMenu;

    public ProjectMemberViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        popupMenu = new PopupMenu(itemView.getContext(), buttonOverflow);
        popupMenu.getMenuInflater().inflate(R.menu.item_menu_project_member, popupMenu.getMenu());

        buttonOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    public void bind(Member member) {
        textUsername.setText(member.getUsername());
        textAccess.setText(Member.getAccessLevel(member.getAccessLevel()));

        App.get().getPicasso()
                .load(ImageUtil.getAvatarUrl(member, itemView.getResources().getDimensionPixelSize(R.dimen.user_header_image_size)))
                .into(image);
    }
}
