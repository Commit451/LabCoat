package com.commit451.gitlab.viewHolders;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows a project member
 * Created by Jawn on 12/19/2015.
 */
public class MemberProjectViewHolder extends RecyclerView.ViewHolder{

    public static MemberProjectViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_project, parent, false);
        return new MemberProjectViewHolder(view);
    }

    @Bind(R.id.overflow) public View overflow;
    @Bind(R.id.name) public TextView username;
    @Bind(R.id.access) public TextView access;
    @Bind(R.id.image) public ImageView image;
    public PopupMenu popupMenu;

    private final View.OnClickListener mOnMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            popupMenu.show();
        }
    };

    public MemberProjectViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        popupMenu = new PopupMenu(itemView.getContext(), overflow);
        popupMenu.getMenuInflater().inflate(R.menu.item_menu_project_member, popupMenu.getMenu());
        overflow.setOnClickListener(mOnMoreClickListener);
    }

    public void bind(User user) {
        username.setText(user.getUsername());
        access.setText(user.getAccessLevelTitle());

        Uri url = ImageUtil.getAvatarUrl(user, itemView.getResources().getDimensionPixelSize(R.dimen.user_header_image_size));
        GitLabClient.getPicasso()
                .load(url)
                .into(image);
    }
}
