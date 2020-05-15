package com.commit451.gitlab.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import com.afollestad.materialdialogs.color.ColorChooserDialog
import com.commit451.addendum.design.snackbar
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.checkValid
import com.commit451.gitlab.extension.text
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.util.ColorUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_new_label.*
import kotlinx.android.synthetic.main.progress_fullscreen.*
import retrofit2.HttpException
import timber.log.Timber

/**
 * Create a brand new label
 */
class AddNewLabelActivity : BaseActivity(), ColorChooserDialog.ColorCallback {

    companion object {

        private const val KEY_PROJECT_ID = "project_id"

        const val KEY_NEW_LABEL = "new_label"

        fun newIntent(context: Context, projectId: Long): Intent {
            val intent = Intent(context, AddNewLabelActivity::class.java)
            intent.putExtra(KEY_PROJECT_ID, projectId)
            return intent
        }
    }

    var chosenColor = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_label)

        rootColor.setOnClickListener {
            // Pass AppCompatActivity which implements ColorCallback, along with the textTitle of the dialog
            ColorChooserDialog.Builder(this, R.string.add_new_label_choose_color)
                    .preselect(chosenColor)
                    .show(this)
        }

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        toolbar.inflateMenu(R.menu.create)
        toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_create -> {
                    createLabel()
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
    }

    override fun onColorSelection(dialog: ColorChooserDialog, @ColorInt selectedColor: Int) {
        chosenColor = selectedColor
        imageColor.setImageDrawable(ColorDrawable(selectedColor))
    }

    override fun onColorChooserDismissed(dialog: ColorChooserDialog) {
    }

    private val projectId: Long
        get() = intent.getLongExtra(KEY_PROJECT_ID, -1)

    private fun createLabel() {
        val valid = textInputLayoutTitle.checkValid()
        if (valid) {
            if (chosenColor == -1) {
                root.snackbar(R.string.add_new_label_color_is_required)
                return
            }
            val title = textInputLayoutTitle.text()
            var description: String? = null
            if (!textDescription.text.isNullOrEmpty()) {
                description = textDescription.text.toString()
            }
            var color: String? = null
            if (chosenColor != -1) {
                color = ColorUtil.convertColorIntToString(chosenColor)
                Timber.d("Setting color to %s", color)
            }
            progress.visibility = View.VISIBLE
            progress.alpha = 0.0f
            progress.animate().alpha(1.0f)
            App.get().gitLab.createLabel(projectId, title, color, description)
                    .with(this)
                    .subscribe({
                        val data = Intent()
                        data.putExtra(KEY_NEW_LABEL, it.body())
                        setResult(Activity.RESULT_OK, data)
                        finish()
                    }, {
                        Timber.e(it)
                        progress.visibility = View.GONE
                        if (it is HttpException && it.response()?.code() == 409) {
                            root.snackbar(R.string.label_already_exists)
                        } else {
                            root.snackbar(R.string.failed_to_create_label)
                        }
                    })
        }
    }
}
