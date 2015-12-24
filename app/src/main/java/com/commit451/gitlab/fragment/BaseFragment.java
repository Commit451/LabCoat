package com.commit451.gitlab.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.event.ReloadDataEvent;
import com.squareup.otto.Subscribe;

/**
 * Created by Jawn on 9/1/2015.
 */
public class BaseFragment extends Fragment{

    EventReceiver mBaseEventReceiever;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBaseEventReceiever = new EventReceiver();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GitLabApp.bus().register(mBaseEventReceiever);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        GitLabApp.bus().unregister(mBaseEventReceiever);
    }

    protected void loadData() {
        //Override this
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
