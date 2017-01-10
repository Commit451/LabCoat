package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.View

import butterknife.ButterKnife
import butterknife.Unbinder

/**
 * Takes care of binding and unbinding
 */
open class ButterKnifeFragment : BaseFragment() {

    var unbinder: Unbinder? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unbinder = ButterKnife.bind(this, view!!)
    }

    override fun onDestroyView() {
        unbinder?.unbind()
        super.onDestroyView()
    }
}
