package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.DiffAdapter
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Diff
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryCommit
import timber.log.Timber

/**
 * Shows the lines of a commit aka the diff
 */
class DiffActivity : BaseActivity() {

    companion object {

        private const val EXTRA_PROJECT = "extra_project"
        private const val EXTRA_COMMIT = "extra_commit"

        fun newIntent(context: Context, project: Project, commit: RepositoryCommit): Intent {
            val intent = Intent(context, DiffActivity::class.java)
            intent.putExtra(EXTRA_PROJECT, project)
            intent.putExtra(EXTRA_COMMIT, commit)
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

        project = intent.getParcelableExtra(EXTRA_PROJECT)!!
        commit = intent.getParcelableExtra(EXTRA_COMMIT)!!

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.title = commit.shortId

        adapterDiff = DiffAdapter(commit, object : DiffAdapter.Listener {
            override fun onDiffClicked(diff: Diff) {

            }
        })
        listDiff.adapter = adapterDiff
        listDiff.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        swipeRefreshLayout.setOnRefreshListener { loadData() }

        loadData()
    }

    fun loadData() {
        textMessage.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = true
        App.get().gitLab.getCommitDiff(project.id, commit.id)
                .with(this)
                .subscribe({
                    swipeRefreshLayout.isRefreshing = false
                    adapterDiff.setData(it)
                }, {
                    swipeRefreshLayout.isRefreshing = false
                    Timber.e(it)
                    textMessage.setText(R.string.connection_error)
                    textMessage.visibility = View.VISIBLE
                })
    }
}
