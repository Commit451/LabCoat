package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.event.BuildChangedEvent
import com.commit451.gitlab.extension.getRawBuildUrl
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Build
import com.commit451.gitlab.model.api.Project
import kotlinx.android.synthetic.main.fragment_build_log.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Shows the build log
 */
class BuildLogFragment : BaseFragment() {

    companion object {

        private const val KEY_PROJECT = "project"
        private const val KEY_BUILD = "build"

        fun newInstance(project: Project, build: Build): BuildLogFragment {
            val fragment = BuildLogFragment()
            val args = Bundle()
            args.putParcelable(KEY_PROJECT, project)
            args.putParcelable(KEY_BUILD, build)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var project: Project
    private lateinit var build: Build

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments?.getParcelable(KEY_PROJECT)!!
        build = arguments?.getParcelable(KEY_BUILD)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_build_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonTop.setOnClickListener {
            scrollView.smoothScrollTo(0, 0)
        }
        buttonBottom.setOnClickListener {
            scrollView.smoothScrollTo(0, Int.MAX_VALUE)
        }
        swipeRefreshLayout.setOnRefreshListener { loadData() }
        loadData()
        App.bus().register(this)
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    override fun loadData() {
        if (view == null) {
            return
        }

        swipeRefreshLayout.isRefreshing = true

        val url = build.getRawBuildUrl(App.get().getAccount().serverUrl!!, project)

        App.get().gitLab.getRaw(url)
                .with(this)
                .subscribe({
                    swipeRefreshLayout.isRefreshing = false
                    textLog.text = it
                }, {
                    Timber.e(it)
                    swipeRefreshLayout.isRefreshing = false
                    textMessage.visibility = View.VISIBLE
                })
    }

    @Suppress("unused")
    @Subscribe
    fun onBuildChanged(event: BuildChangedEvent) {
        if (build.id == event.build.id) {
            build = event.build
            loadData()
        }
    }
}
