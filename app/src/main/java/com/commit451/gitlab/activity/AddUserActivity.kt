package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import com.commit451.addendum.design.snackbar
import com.commit451.alakazam.fadeOut
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.dialog.AccessDialog
import com.commit451.gitlab.event.MemberAddedEvent
import com.commit451.gitlab.extension.mapResponseSuccess
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.CommitViewHolder
import com.commit451.gitlab.viewHolder.UserViewHolder
import com.commit451.teleprinter.Teleprinter
import io.reactivex.Single
import kotlinx.android.synthetic.main.activity_add_user.*
import kotlinx.android.synthetic.main.activity_add_user.swipeRefreshLayout
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber

/**
 * Add a new user to the repo or to the group, depending on the mode
 */
class AddUserActivity : MorphActivity() {

    companion object {

        private const val KEY_PROJECT_ID = "project_id"
        private const val KEY_GROUP = "group"

        fun newIntent(context: Context, projectId: Long): Intent {
            val intent = Intent(context, AddUserActivity::class.java)
            intent.putExtra(KEY_PROJECT_ID, projectId)
            return intent
        }

        fun newIntent(context: Context, group: Group): Intent {
            val intent = Intent(context, AddUserActivity::class.java)
            intent.putExtra(KEY_GROUP, group)
            return intent
        }
    }

    private lateinit var adapter: BaseAdapter<User, UserViewHolder>
    private lateinit var loadHelper: LoadHelper<User>
    private lateinit var dialogAccess: AccessDialog
    private lateinit var teleprinter: Teleprinter

    private var projectId: Long = 0
    private var group: Group? = null
    private var query: String? = null
    private var selectedUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)
        teleprinter = Teleprinter(this)
        projectId = intent.getLongExtra(KEY_PROJECT_ID, -1)
        group = intent.getParcelableExtra(KEY_GROUP)
        dialogAccess = AccessDialog(this, object : AccessDialog.Listener {
            override fun onAccessApplied(accessLevel: Int) {
                dialogAccess.showLoading()
                val group = group
                if (group == null) {
                    add(gitLab.addProjectMember(projectId, selectedUser!!.id, accessLevel))
                } else {
                    add(gitLab.addGroupMember(group.id, selectedUser!!.id, accessLevel))
                }
            }
        })
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val spanCount = 2
        val layoutManager = GridLayoutManager(this, spanCount)
        layoutManager.spanSizeLookup = BaseAdapter.createSpanSizeLookup(spanCount) { adapter }
        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    val viewHolder = CommitViewHolder.inflate(parent)
                    viewHolder.itemView.setOnClickListener {
                        val user = adapter.items[viewHolder.adapterPosition]
                        selectedUser = user
                        dialogAccess.show()
                    }
                    viewHolder
                },
                onBindViewHolder = { viewHolder, _, item -> viewHolder.bind(item) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = list,
                baseAdapter = adapter,
                layoutManager = layoutManager,
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = {
                    gitLab.searchUsers(query!!)
                },
                loadMore = {
                    gitLab.loadAnyList(it)
                }
        )

        morph(root)
        textSearch.requestFocus()
        textSearch.setOnEditorActionListener { _, _, _ ->
            if (!textSearch.text.isNullOrEmpty()) {
                query = textSearch.text.toString()
                loadData()
            }
            true
        }
        textSearch.addTextChangedListener {
            if (it.isNullOrBlank()) {
                buttonClear.fadeOut()
            } else {
                buttonClear.visibility = View.VISIBLE
                buttonClear.animate().alpha(1.0f)
            }
        }
        buttonClear.setOnClickListener {
            buttonClear.animate().alpha(0.0f).withEndAction {
                buttonClear.visibility = View.GONE
                textSearch.text.clear()
                teleprinter.showKeyboard(textSearch)
            }
        }
    }

    private fun loadData() {
        loadHelper.load()
        teleprinter.hideKeyboard()
    }

    private fun add(observable: Single<Response<User>>) {
        observable
                .mapResponseSuccess()
                .with(this)
                .subscribe({
                    root.snackbar(R.string.user_added_successfully)
                    dialogAccess.dismiss()
                    dismiss()
                    App.bus().post(MemberAddedEvent(it))
                }, {
                    Timber.e(it)
                    var message = getString(R.string.error_failed_to_add_user)
                    if (it is HttpException) {
                        when (it.code()) {
                            409 -> message = getString(R.string.error_user_conflict)
                        }
                    }
                    root.snackbar(message)
                })
    }
}
