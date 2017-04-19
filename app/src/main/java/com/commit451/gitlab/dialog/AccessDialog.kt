package com.commit451.gitlab.dialog

import android.content.Context
import android.widget.Toast
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.model.api.Member
import com.commit451.gitlab.rx.CustomSingleObserver
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*

/**
 * Change a users access level, either for a group or for a project
 */
class AccessDialog private constructor(context: Context, internal var member: Member?, internal var group: Group?, projectId: Long) : MaterialDialog(MaterialDialog.Builder(context)
        .items(if (group == null) R.array.project_role_names else R.array.group_role_names)
        .itemsCallbackSingleChoice(-1) { materialDialog, view, i, charSequence -> true }
        .theme(Theme.DARK)
        .progress(true, 0) // So we can later show loading progress
        .positiveText(R.string.action_apply)
        .negativeText(R.string.md_cancel_label)) {

    private var onAccessChangedListener: OnAccessChangedListener? = null
    var listener: Listener? = null

    var roleNames: Array<String> = getContext().resources.getStringArray(if (group == null)
        R.array.project_role_names
    else
        R.array.group_role_names)
    var projectId: Long = -1

    constructor(context: Context, accessAppliedListener: Listener) : this(context, null, null, -1) {
        listener = accessAppliedListener
    }

    constructor(context: Context, member: Member, group: Group) : this(context, member, group, -1)

    constructor(context: Context, member: Member, projectId: Long) : this(context, member, null, projectId)

    init {
        getActionButton(DialogAction.POSITIVE).setOnClickListener { onApply() }
        getActionButton(DialogAction.NEGATIVE).setOnClickListener { onCancel() }
        this.projectId = projectId
        if (this.member != null) {
            selectedIndex = Arrays.asList(*roleNames).indexOf(
                    Member.getAccessLevel(this.member!!.accessLevel))
        }
    }

    fun changeAccess(accessLevel: Int) {

        if (group != null) {
            showLoading()
            editGroupOrProjectMember(App.get().gitLab.editGroupMember(group!!.id, member!!.id, accessLevel))
        } else if (projectId != -1L) {
            showLoading()
            editGroupOrProjectMember(App.get().gitLab.editProjectMember(projectId, member!!.id, accessLevel))
        } else if (listener != null) {
            listener!!.onAccessApplied(accessLevel)
        } else {
            throw IllegalStateException("Not sure what to apply this access change to. Check the constructors plz")
        }
    }

    fun editGroupOrProjectMember(observable: Single<Member>) {
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomSingleObserver<Member>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        this@AccessDialog.onError()
                    }

                    override fun success(member: Member) {
                        if (onAccessChangedListener != null) {
                            onAccessChangedListener!!.onAccessChanged(this@AccessDialog.member!!, roleNames[selectedIndex])
                        }
                        dismiss()
                    }
                })
    }

    fun showLoading() {
        getActionButton(DialogAction.POSITIVE).isEnabled = false
    }

    fun onError() {
        Toast.makeText(context, R.string.failed_to_apply_access_level, Toast.LENGTH_SHORT).show()
        dismiss()
    }

    fun setOnAccessChangedListener(listener: OnAccessChangedListener) {
        onAccessChangedListener = listener
    }

    fun onApply() {
        if (selectedIndex == -1) {
            Toast.makeText(context, R.string.please_select_access_level, Toast.LENGTH_LONG)
                    .show()
            return
        }
        val accessLevel = roleNames[selectedIndex]
        if (accessLevel == null) {
            Toast.makeText(context, R.string.please_select_access_level, Toast.LENGTH_LONG)
                    .show()
        } else {
            changeAccess(Member.getAccessLevel(accessLevel))
        }
    }

    fun onCancel() {
        dismiss()
    }

    interface OnAccessChangedListener {
        fun onAccessChanged(member: Member, accessLevel: String)
    }

    interface Listener {
        fun onAccessApplied(accessLevel: Int)
    }
}
