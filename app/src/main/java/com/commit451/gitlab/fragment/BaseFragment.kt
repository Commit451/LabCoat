package com.commit451.gitlab.fragment


import android.os.Bundle
import android.view.View

import com.commit451.gitlab.App
import com.commit451.gitlab.event.ReloadDataEvent
import com.trello.rxlifecycle2.components.support.RxFragment

import org.greenrobot.eventbus.Subscribe


open class BaseFragment : RxFragment() {

    var baseEventReceiver: EventReceiver? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseEventReceiver = EventReceiver()
        App.bus().register(baseEventReceiver!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        App.bus().unregister(baseEventReceiver)
    }

    /**
     * Load the data based on a [ReloadDataEvent]
     */
    open fun loadData() {
    }

    open fun onBackPressed(): Boolean {
        return false
    }

    inner class EventReceiver {
        @Subscribe
        fun onReloadData(event: ReloadDataEvent) {
            loadData()
        }
    }
}
