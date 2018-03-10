package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.addendum.parceler.getParcelerParcelableExtra
import com.commit451.addendum.parceler.putParcelerParcelableExtra
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.DiffAdapter
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Diff
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryCommit
import com.commit451.gitlab.rx.CustomSingleObserver
import timber.log.Timber

/**
 * Shows the lines of a commit aka the diff
 */
class DiffActivity : BaseActivity() {

    companion object {

        private val EXTRA_PROJECT = "extra_project"
        private val EXTRA_COMMIT = "extra_commit"

        fun newIntent(context: Context, project: Project, commit: RepositoryCommit): Intent {
            val intent = Intent(context, DiffActivity::class.java)
            intent.putParcelerParcelableExtra(EXTRA_PROJECT, project)
            intent.putParcelerParcelableExtra(EXTRA_COMMIT, commit)
            return intent
        }
    }

    @BindView(R.id.root)
    lateinit var root: ViewGroup
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.swipe_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list)
    lateinit var listDiff: RecyclerView
    @BindView(R.id.message_text)
    lateinit var textMessage: TextView

    lateinit var adapterDiff: DiffAdapter

    lateinit var project: Project
    lateinit var commit: RepositoryCommit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diff)
        ButterKnife.bind(this)

        project = intent.getParcelerParcelableExtra<Project>(EXTRA_PROJECT)!!
        commit = intent.getParcelerParcelableExtra<RepositoryCommit>(EXTRA_COMMIT)!!

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.title = commit.shortId

        adapterDiff = DiffAdapter(commit, object : DiffAdapter.Listener {
            override fun onDiffClicked(diff: Diff) {

            }
        })
        listDiff.adapter = adapterDiff
        listDiff.layoutManager = LinearLayoutManager(this)
        swipeRefreshLayout.setOnRefreshListener { loadData() }

        loadData()
    }

    fun loadData() {
        textMessage.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = true
        App.get().gitLab.getCommitDiff(project.id, commit.id)
                .with(this)
                .subscribe(object : CustomSingleObserver<List<Diff>>() {

                    override fun error(t: Throwable) {
                        swipeRefreshLayout.isRefreshing = false
                        Timber.e(t)
                        textMessage.setText(R.string.connection_error)
                        textMessage.visibility = View.VISIBLE
                    }

                    override fun success(diffs: List<Diff>) {
                        swipeRefreshLayout.isRefreshing = false
                        adapterDiff.setData(diffs)
                    }
                })
    }
}