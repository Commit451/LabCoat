package com.commit451.gitlab.dialog;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.event.LogoutEvent;
import com.commit451.gitlab.util.NavigationManager;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Log out confirmation
 * Created by Jawn on 8/21/2015.
 */
public class LogoutDialog extends AppCompatDialog {

    @OnClick(R.id.logout_button)
    void onLogoutClick() {
        //TODO figure out what to do when logging out
        NavigationManager.navigateToLogin(getContext());
        GitLabApp.bus().post(new LogoutEvent());
    }

    @OnClick(R.id.cancel_button)
    void onCancelClick() {
        dismiss();
    }

    public LogoutDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_logout);
        ButterKnife.bind(this);
    }
}
