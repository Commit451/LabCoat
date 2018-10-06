package com.commit451.gitlab.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.commit451.gitlab.R
import com.commit451.gitlab.fragment.PickBranchFragment
import com.commit451.gitlab.fragment.PickTagFragment
import com.commit451.gitlab.model.Ref

/**
 * Projects Pager Adapter
 */
class PickBranchOrTagPagerAdapter(context: Context, fm: androidx.fragment.app.FragmentManager, private val projectId: Long, private val ref: Ref?) : androidx.fragment.app.FragmentPagerAdapter(fm) {

    private val titles: Array<String> = context.resources.getStringArray(R.array.tabs_branch_tag)

    override fun getItem(position: Int): androidx.fragment.app.Fragment {

        when (position) {
            0 -> return PickBranchFragment.newInstance(projectId, ref)
            1 -> return PickTagFragment.newInstance(projectId, ref)
        }

        throw IllegalStateException("Position exceeded on view pager")
    }

    override fun getCount(): Int {
        return titles.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }
}
