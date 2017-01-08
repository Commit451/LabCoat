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
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Label;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.util.ColorUtil;
import com.commit451.gitlab.util.Validator;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
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
    ViewGroup root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_text_input_layout)
    TextInputLayout textInputLayoutTitle;
    @BindView(R.id.description)
    TextView textDescription;
    @BindView(R.id.image_color)
    ImageView imageColor;
    @BindView(R.id.progress)
    View progress;

    int chosenColor = -1;

    @OnClick(R.id.root_color)
    void onChooseColorClicked() {
        // Pass AppCompatActivity which implements ColorCallback, along with the textTitle of the dialog
        new ColorChooserDialog.Builder(this, R.string.add_new_label_choose_color)
                .preselect(chosenColor)
                .show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_label);
        ButterKnife.bind(this);

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        toolbar.inflateMenu(R.menu.menu_add_new_label);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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
        chosenColor = selectedColor;
        imageColor.setImageDrawable(new ColorDrawable(selectedColor));
    }

    private long getProjectId() {
        return getIntent().getLongExtra(KEY_PROJECT_ID, -1);
    }

    private void createLabel() {
        if (Validator.validateFieldsNotEmpty(getString(R.string.required_field), textInputLayoutTitle)) {
            if (chosenColor == -1) {
                Snackbar.make(root, R.string.add_new_label_color_is_required, Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            String title = textInputLayoutTitle.getEditText().getText().toString();
            String description = null;
            if (!TextUtils.isEmpty(textDescription.getText())) {
                description = textDescription.getText().toString();
            }
            String color = null;
            if (chosenColor != -1) {
                color = ColorUtil.convertColorIntToString(chosenColor);
                Timber.d("Setting color to %s", color);
            }
            progress.setVisibility(View.VISIBLE);
            progress.setAlpha(0.0f);
            progress.animate().alpha(1.0f);
            App.get().getGitLab().createLabel(getProjectId(), title, color, description)
                    .compose(this.<Response<Label>>bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CustomResponseSingleObserver<Label>() {

                        @Override
                        public void error(@NonNull Throwable e) {
                            Timber.e(e);
                            progress.setVisibility(View.GONE);
                            if (e instanceof HttpException && ((HttpException) e).response().code() == 409) {
                                Snackbar.make(root, R.string.label_already_exists, Snackbar.LENGTH_SHORT)
                                        .show();
                            } else {
                                Snackbar.make(root, R.string.failed_to_create_label, Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        }

                        @Override
                        public void responseSuccess(@NonNull Label label) {
                            Intent data = new Intent();
                            data.putExtra(KEY_NEW_LABEL, Parcels.wrap(label));
                            setResult(RESULT_OK, data);
                            finish();
                        }
                    });
        }
    }
}
