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
public class ProjectMemberViewHolder extends RecyclerView.ViewHolder{

    public static ProjectMemberViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_project, parent, false);
        return new ProjectMemberViewHolder(view);
    }

    @BindView(R.id.overflow) View mOverflowView;
    @BindView(R.id.name) TextView mUsernameView;
    @BindView(R.id.access) TextView mAccessView;
    @BindView(R.id.image) public ImageView mImageView;

    public final PopupMenu mPopupMenu;

    public ProjectMemberViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        mPopupMenu = new PopupMenu(itemView.getContext(), mOverflowView);
        mPopupMenu.getMenuInflater().inflate(R.menu.item_menu_project_member, mPopupMenu.getMenu());

        mOverflowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupMenu.show();
            }
        });
    }

    public void bind(Member member) {
        mUsernameView.setText(member.getUsername());
        mAccessView.setText(Member.getAccessLevel(member.getAccessLevel()));

        App.get().getPicasso()
                .load(ImageUtil.getAvatarUrl(member, itemView.getResources().getDimensionPixelSize(R.dimen.user_header_image_size)))
                .into(mImageView);
    }
}
