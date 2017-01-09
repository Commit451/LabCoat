package com.commit451.gitlab.fragment

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.event.BuildChangedEvent
import com.commit451.gitlab.model.api.Build
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.util.BuildUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.Subscribe
import org.parceler.Parcels
import timber.log.Timber

/**
 * Shows the build artifacts
 */
class BuildLogFragment : ButterKnifeFragment() {

    companion object {

        private val KEY_PROJECT = "project"
        private val KEY_BUILD = "build"

        fun newInstance(project: Project, build: Build): BuildLogFragment {
            val fragment = BuildLogFragment()
            val args = Bundle()
            args.putParcelable(KEY_PROJECT, Parcels.wrap(project))
            args.putParcelable(KEY_BUILD, Parcels.wrap(build))
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.swipe_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.log)
    lateinit var textLog: TextView
    @BindView(R.id.message_text)
    lateinit var textMessage: TextView

    lateinit var project: Project
    lateinit var build: Build

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = Parcels.unwrap<Project>(arguments.getParcelable<Parcelable>(KEY_PROJECT))
        build = Parcels.unwrap<Build>(arguments.getParcelable<Parcelable>(KEY_BUILD))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_build_log, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        val url = BuildUtil.getRawBuildUrl(App.get().account.serverUrl, project, build)

        App.get().gitLab.getRaw(url)
                .compose(this.bindToLifecycle<String>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomSingleObserver<String>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                    }

                    override fun success(log: String) {
                        swipeRefreshLayout.isRefreshing = false
                        textLog.text = log
                    }
                })
    }

    @Subscribe
    fun onBuildChanged(event: BuildChangedEvent) {
        if (build.id == event.build.id) {
            build = event.build
            loadData()
        }
    }
}