package com.commit451.gitlab.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.ProjectsAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.dialogs.LogoutDialog;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.tkeunebr.gravatar.Gravatar;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

/**
 * Our very own navigation view
 * Created by Jawn on 7/28/2015.
 */
public class GitLabNavigationView extends FrameLayout {

    @Bind(R.id.profile_image) ImageView profileImage;
    @Bind(R.id.profile_user) TextView userName;
    @Bind(R.id.profile_email) TextView userEmail;
    @Bind(R.id.list) RecyclerView projectList;
    @Bind(R.id.drawer_header) FrameLayout header;
    ProjectsAdapter mAdapter;

    private Drawable mInsetForeground;
    private Rect mInsets;
    private Rect mTempRect = new Rect();

    @OnClick(R.id.drawer_header)
    void onHeaderClick() {
        new LogoutDialog(getContext()).show();
    }

    private final Callback<User> userCallback = new Callback<User>() {

        @Override
        public void onResponse(Response<User> response) {
            if (!response.isSuccess()) {
                return;
            }
            bindUser(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t.toString());
        }
    };

    public GitLabNavigationView(Context context) {
        super(context);
        init();
    }

    public GitLabNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GitLabNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public GitLabNavigationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.nav_drawer, this);
        ButterKnife.bind(this);
        mAdapter = new ProjectsAdapter();
        projectList.setAdapter(mAdapter);
        projectList.setLayoutManager(new LinearLayoutManager(getContext()));
        mInsetForeground = new ColorDrawable(Color.parseColor("#44000000"));
    }

    public void setProjects(List<Project> projects) {
        mAdapter.setData(projects);
    }

    public void loadCurrentUser() {
        GitLabClient.instance().getUser().enqueue(userCallback);
    }

    private void bindUser(User user) {
        if (getContext() == null) {
            return;
        }
        if (user.getUsername() != null) {
            userName.setText(user.getUsername());
        }
        int size = getResources().getDimensionPixelSize(R.dimen.larger_image_size);
        String url = "http://www.gravatar.com/avatar/00000000000000000000000000000000?s=" + size;
        if(user.getEmail() != null) {
            url = Gravatar.init().with(user.getEmail()).size(size).build();
            userEmail.setText(user.getEmail());
        }
        Picasso.with(getContext())
                .load(url)
                .into(profileImage);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        mInsets = new Rect(insets);
        setWillNotDraw(mInsetForeground == null);
        ViewCompat.postInvalidateOnAnimation(this);

        int headerHeight = getResources().getDimensionPixelSize(R.dimen.navigation_drawer_header_height);
        ViewGroup.LayoutParams lp2 = header.getLayoutParams();
        lp2.height = headerHeight + insets.top;
        header.setLayoutParams(lp2);

        MarginLayoutParams params = (MarginLayoutParams) profileImage.getLayoutParams();
        params.topMargin = (int) (insets.top + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
        profileImage.setLayoutParams(params);

        return true; // consume insets
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (mInsets != null && mInsetForeground != null) {
            int sc = canvas.save();
            canvas.translate(getScrollX(), getScrollY());

            // Top
            mTempRect.set(0, 0, width, mInsets.top);
            mInsetForeground.setBounds(mTempRect);
            mInsetForeground.draw(canvas);

            // Bottom
            mTempRect.set(0, height - mInsets.bottom, width, height);
            mInsetForeground.setBounds(mTempRect);
            mInsetForeground.draw(canvas);

            // Left
            mTempRect.set(0, mInsets.top, mInsets.left, height - mInsets.bottom);
            mInsetForeground.setBounds(mTempRect);
            mInsetForeground.draw(canvas);

            // Right
            mTempRect.set(width - mInsets.right, mInsets.top, width, height - mInsets.bottom);
            mInsetForeground.setBounds(mTempRect);
            mInsetForeground.draw(canvas);

            canvas.restoreToCount(sc);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mInsetForeground != null) {
            mInsetForeground.setCallback(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mInsetForeground != null) {
            mInsetForeground.setCallback(null);
        }
    }

}
