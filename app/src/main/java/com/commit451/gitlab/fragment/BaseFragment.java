package com.commit451.gitlab.fragment;


import android.os.Bundle;
import android.view.View;

import com.commit451.gitlab.App;
import com.commit451.gitlab.event.ReloadDataEvent;
import com.trello.rxlifecycle.components.support.RxFragment;

import org.greenrobot.eventbus.Subscribe;


public class BaseFragment extends RxFragment {

    private EventReceiver mBaseEventReceiever;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBaseEventReceiever = new EventReceiver();
        App.bus().register(mBaseEventReceiever);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.bus().unregister(mBaseEventReceiever);
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
