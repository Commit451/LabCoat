package com.commit451.gitlab.fragment;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.event.ReloadDataEvent;
import com.squareup.otto.Subscribe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

public class BaseFragment extends Fragment{

    private EventReceiver mBaseEventReceiever;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBaseEventReceiever = new EventReceiver();
        GitLabApp.bus().register(mBaseEventReceiever);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        GitLabApp.bus().unregister(mBaseEventReceiever);
    }

    protected void loadData() {
    }

    public boolean onBackPressed() {
        return false;
    }

    private class EventReceiver {
        @Subscribe
        public void onReloadData(ReloadDataEvent event) {
            loadData();
        }
    }
}
