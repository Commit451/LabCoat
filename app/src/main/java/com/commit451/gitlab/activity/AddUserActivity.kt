package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commit451.addendum.design.snackbar
import com.commit451.alakazam.fadeOut
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.UserAdapter
import com.commit451.gitlab.dialog.AccessDialog
import com.commit451.gitlab.event.MemberAddedEvent
import com.commit451.gitlab.extension.mapResponseSuccess
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.viewHolder.UserViewHolder
import com.commit451.teleprinter.Teleprinter
import io.reactivex.Single
import kotlinx.android.synthetic.main.activity_add_user.*
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

    private lateinit var layoutManager: GridLayoutManager
    private lateinit var adapter: UserAdapter
    private lateinit var dialogAccess: AccessDialog
    private lateinit var teleprinter: Teleprinter

    private var projectId: Long = 0
    private var group: Group? = null
    private var query: String? = null
    private var nextPageUrl: Uri? = null
    private var loading = false
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
                    add(App.get().gitLab.addProjectMember(projectId, selectedUser!!.id, accessLevel))
                } else {
                    add(App.get().gitLab.addGroupMember(group.id, selectedUser!!.id, accessLevel))
                }
            }
        })
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        adapter = UserAdapter(object : UserAdapter.Listener {
            override fun onUserClicked(user: User, userViewHolder: UserViewHolder) {
                selectedUser = user
                dialogAccess.show()
            }
        })
        swipeRefreshLayout.setOnRefreshListener { loadData() }
        list.adapter = adapter
        layoutManager = GridLayoutManager(this, 2)
        layoutManager.spanSizeLookup = adapter.spanSizeLookup
        list.layoutManager = layoutManager
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                    loadMore()
                }
            }
        })

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
        teleprinter.hideKeyboard()
        swipeRefreshLayout.isRefreshing = true
        loading = true
        App.get().gitLab.searchUsers(query!!)
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    swipeRefreshLayout.isRefreshing = false
                    loading = false
                    adapter.setData(it.body)
                    nextPageUrl = it.paginationData.next
                    Timber.d("Next page url is %s", nextPageUrl)
                }, {
                    Timber.e(it)
                    swipeRefreshLayout.isRefreshing = false
                    loading = false
                    root.snackbar(getString(R.string.connection_error_users))
                })
    }

    private fun loadMore() {
        loading = true
        adapter.setLoading(true)
        Timber.d("loadMore " + nextPageUrl!!.toString() + " " + query)
        App.get().gitLab.searchUsers(nextPageUrl!!.toString(), query!!)
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    adapter.setLoading(false)
                    adapter.addData(it.body)
                    nextPageUrl = it.paginationData.next
                }, {
                    Timber.e(it)
                    adapter.setLoading(false)
                })
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
