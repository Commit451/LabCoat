package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.commit451.easycallback.EasyCallback;
import com.commit451.easycallback.HttpException;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Label;
import com.commit451.gitlab.util.ColorUtil;
import com.commit451.gitlab.util.Validator;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Create a brand new label
 */
public class AddNewLabelActivity extends BaseActivity implements ColorChooserDialog.ColorCallback {

    private static final String KEY_PROJECT_ID = "project_id";

    public static final String KEY_NEW_LABEL = "new_label";

    public static Intent newIntent(Context context, long projectId) {
        Intent intent = new Intent(context, AddNewLabelActivity.class);
        intent.putExtra(KEY_PROJECT_ID, projectId);
        return intent;
    }

    @BindView(R.id.root)
    ViewGroup mRoot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.title_text_input_layout)
    TextInputLayout mTextInputLayoutTitle;
    @BindView(R.id.description)
    TextView mDescription;
    @BindView(R.id.image_color)
    ImageView mImageColor;
    @BindView(R.id.progress)
    View mProgress;

    int mChosenColor = -1;

    private final EasyCallback<Label> mCreateLabelCallback = new EasyCallback<Label>() {
        @Override
        public void success(@NonNull Label response) {
            Intent data = new Intent();
            data.putExtra(KEY_NEW_LABEL, Parcels.wrap(response));
            setResult(RESULT_OK, data);
            finish();
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            if (t instanceof HttpException && ((HttpException) t).response().code() == 409) {
                Snackbar.make(mRoot, R.string.label_already_exists, Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                Snackbar.make(mRoot, R.string.failed_to_create_label, Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    };

    @OnClick(R.id.root_color)
    void onChooseColorClicked() {
        // Pass AppCompatActivity which implements ColorCallback, along with the title of the dialog
        new ColorChooserDialog.Builder(this, R.string.add_new_label_choose_color)
                .preselect(mChosenColor)
                .show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_label);
        ButterKnife.bind(this);

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mToolbar.inflateMenu(R.menu.menu_add_new_label);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_create:
                        createLabel();
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        mChosenColor = selectedColor;
        mImageColor.setImageDrawable(new ColorDrawable(selectedColor));
    }

    private long getProjectId() {
        return getIntent().getLongExtra(KEY_PROJECT_ID, -1);
    }

    private void createLabel() {
        if (Validator.validateFieldsNotEmpty(getString(R.string.required_field), mTextInputLayoutTitle)) {
            if (mChosenColor == -1) {
                Snackbar.make(mRoot, R.string.add_new_label_color_is_required, Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            String title = mTextInputLayoutTitle.getEditText().getText().toString();
            String description = null;
            if (!TextUtils.isEmpty(mDescription.getText())) {
                description = mDescription.getText().toString();
            }
            String color = null;
            if (mChosenColor != -1) {
                color = ColorUtil.convertColorIntToString(mChosenColor);
                Timber.d("Setting color to %s", color);
            }
            mProgress.setVisibility(View.VISIBLE);
            mProgress.setAlpha(0.0f);
            mProgress.animate().alpha(1.0f);
            App.instance().getGitLab().createLabel(getProjectId(), title, color, description)
                    .enqueue(mCreateLabelCallback);
        }
    }
}
