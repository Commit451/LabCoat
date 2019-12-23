package com.commit451.gitlab.dialog

import android.content.Context
import android.widget.Toast
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.model.api.User
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * Change a users access level, either for a group or for a project
 */
class AccessDialog private constructor(context: Context, internal var member: User?, internal var group: Group?, projectId: Long) : MaterialDialog(Builder(context)
        .items(if (group == null) R.array.project_role_names else R.array.group_role_names)
        .itemsCallbackSingleChoice(-1) { _, _, _, _ -> true }
        .theme(Theme.DARK)
        .progress(true, 0) // So we can later show loading progress
        .positiveText(R.string.action_apply)
        .negativeText(R.string.cancel)) {

    private var onAccessChangedListener: OnAccessChangedListener? = null
    var listener: Listener? = null

    var roleNames: Array<String> = getContext().resources.getStringArray(if (group == null)
        R.array.project_role_names
    else
        R.array.group_role_names)
    var projectId: Long = -1
    val disposables = CompositeDisposable()

    constructor(context: Context, accessAppliedListener: Listener) : this(context, null, null, -1) {
        listener = accessAppliedListener
    }

    constructor(context: Context, member: User, group: Group) : this(context, member, group, -1)

    constructor(context: Context, member: User, projectId: Long) : this(context, member, null, projectId)

    init {
        getActionButton(DialogAction.POSITIVE).setOnClickListener { onApply() }
        getActionButton(DialogAction.NEGATIVE).setOnClickListener { onCancel() }
        this.projectId = projectId
        if (this.member != null) {
            selectedIndex = listOf(*roleNames).indexOf(
                    User.getAccessLevel(this.member!!.accessLevel))
        }
    }

    private fun changeAccess(accessLevel: Int) {
        when {
            group != null -> {
                showLoading()
                editGroupOrProjectMember(App.get().gitLab.editGroupMember(group!!.id, member!!.id, accessLevel))
            }
            projectId != -1L -> {
                showLoading()
                editGroupOrProjectMember(App.get().gitLab.editProjectMember(projectId, member!!.id, accessLevel))
            }
            listener != null -> {
                listener!!.onAccessApplied(accessLevel)
            }
            else -> {
                throw IllegalStateException("Not sure what to apply this access change to. Check the constructors plz")
            }
        }
    }

    private fun editGroupOrProjectMember(observable: Single<User>) {
        disposables += observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (onAccessChangedListener != null) {
                        onAccessChangedListener!!.onAccessChanged(this@AccessDialog.member!!, roleNames[selectedIndex])
                    }
                    dismiss()
                }, {
                    Timber.e(it)
                    this@AccessDialog.onError()
                })
    }

    fun showLoading() {
        getActionButton(DialogAction.POSITIVE).isEnabled = false
    }

    private fun onError() {
        Toast.makeText(context, R.string.failed_to_apply_access_level, Toast.LENGTH_SHORT).show()
        dismiss()
    }

    fun setOnAccessChangedListener(listener: OnAccessChangedListener) {
        onAccessChangedListener = listener
    }

    private fun onApply() {
        if (selectedIndex == -1) {
            Toast.makeText(context, R.string.please_select_access_level, Toast.LENGTH_LONG)
                    .show()
            return
        }
        val accessLevel = roleNames[selectedIndex]
        changeAccess(User.getAccessLevel(accessLevel))
    }

    private fun onCancel() {
        dismiss()
    }

    override fun onDetachedFromWindow() {
        disposables.clear()
        super.onDetachedFromWindow()
    }

    interface OnAccessChangedListener {
        fun onAccessChanged(member: User, accessLevel: String)
    }

    interface Listener {
        fun onAccessApplied(accessLevel: Int)
    }
}
