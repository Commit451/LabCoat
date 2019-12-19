package com.commit451.gitlab.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.R.string.labels
import com.commit451.gitlab.adapter.LabelAdapter
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Label
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.viewHolder.LabelViewHolder
import timber.log.Timber

/**
 * Add labels!
 */
class AddLabelActivity : BaseActivity() {

    companion object {

        private const val KEY_PROJECT_ID = "project_id"
        private const val REQUEST_NEW_LABEL = 1

        const val KEY_LABEL = "label"

        fun newIntent(context: Context, projectId: Long): Intent {
            val intent = Intent(context, AddLabelActivity::class.java)
            intent.putExtra(KEY_PROJECT_ID, projectId)
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
    lateinit var list: RecyclerView
    @BindView(R.id.message_text)
    lateinit var textMessage: TextView

    lateinit var adapterLabel: LabelAdapter

    var projectId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_label)
        ButterKnife.bind(this)

        projectId = intent.getLongExtra(KEY_PROJECT_ID, -1)
        toolbar.setTitle(labels)
        toolbar.inflateMenu(R.menu.create)
        toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_create -> {
                    Navigator.navigateToAddNewLabel(this@AddLabelActivity, projectId, REQUEST_NEW_LABEL)
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
        swipeRefreshLayout.setOnRefreshListener { load() }
        adapterLabel = LabelAdapter(object : LabelAdapter.Listener {
            override fun onLabelClicked(label: Label, viewHolder: LabelViewHolder) {
                val data = Intent()
                data.putExtra(KEY_LABEL, label)
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        })
        list.adapter = adapterLabel
        list.layoutManager = LinearLayoutManager(this)

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        load()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_NEW_LABEL -> if (resultCode == Activity.RESULT_OK) {
                val newLabel = data?.getParcelableExtra<Label>(AddNewLabelActivity.KEY_NEW_LABEL)!!
                adapterLabel.addLabel(newLabel)
            }
        }
    }

    fun load() {
        textMessage.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = true
        App.get().gitLab.getLabels(projectId)
                .with(this)
                .subscribe(object : CustomSingleObserver<List<Label>>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                    }

                    override fun success(labels: List<Label>) {
                        swipeRefreshLayout.isRefreshing = false
                        if (labels.isEmpty()) {
                            textMessage.visibility = View.VISIBLE
                        }
                        adapterLabel.setItems(labels)
                    }
                })
    }
}
