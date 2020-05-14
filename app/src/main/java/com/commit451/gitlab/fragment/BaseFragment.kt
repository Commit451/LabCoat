package com.commit451.gitlab.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.commit451.gitlab.App
import com.commit451.gitlab.activity.BaseActivity
import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.event.ReloadDataEvent
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import org.greenrobot.eventbus.Subscribe


abstract class BaseFragment : Fragment() {

    private var baseEventReceiver: EventReceiver? = null

    val scopeProvider: AndroidLifecycleScopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    val baseActivty by lazy {
        activity as BaseActivity
    }

    val gitLab: GitLab
        get() = App.get().gitLab

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        @Suppress("unused", "UNUSED_PARAMETER")
        @Subscribe
        fun onReloadData(event: ReloadDataEvent) {
            loadData()
        }
    }
}
