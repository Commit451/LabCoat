package com.commit451.gitlab.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.R.string.labels
import com.commit451.gitlab.adapter.LabelAdapter
import com.commit451.gitlab.model.api.Label
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.viewHolder.LabelViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.parceler.Parcels
import timber.log.Timber

/**
 * Add labels!
 */
class AddLabelActivity : BaseActivity() {

    companion object {

        private val KEY_PROJECT_ID = "project_id"
        private val REQUEST_NEW_LABEL = 1

        val KEY_LABEL = "label"

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
                data.putExtra(KEY_LABEL, Parcels.wrap(label))
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
                val newLabel = Parcels.unwrap<Label>(data?.getParcelableExtra<Parcelable>(AddNewLabelActivity.KEY_NEW_LABEL))
                adapterLabel.addLabel(newLabel)
            }
        }
    }

    fun load() {
        textMessage.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = true
        App.get().gitLab.getLabels(projectId)
                .compose(this.bindToLifecycle<List<Label>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
