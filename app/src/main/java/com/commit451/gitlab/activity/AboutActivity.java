package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gimbal.Gimbal;
import com.commit451.gitlab.App;
import com.commit451.gitlab.BuildConfig;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Contributor;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.util.ImageUtil;
import com.commit451.gitlab.util.IntentUtil;
import com.commit451.gitlab.view.PhysicsFlowLayout;
import com.jawnnypoo.physicslayout.Physics;
import com.jawnnypoo.physicslayout.PhysicsConfig;
import com.wefika.flowlayout.FlowLayout;

import org.jbox2d.common.Vec2;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Thats what its all about
 */
public class AboutActivity extends BaseActivity {
    private static final String REPO_ID = "473568";

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        return intent;
    }

    @BindView(R.id.root)
    ViewGroup root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.contributors)
    TextView textContributors;
    @BindView(R.id.physics_layout)
    PhysicsFlowLayout physicsLayout;
    @BindView(R.id.progress)
    View progress;

    SensorManager sensorManager;
    Sensor gravitySensor;
    Gimbal gimbal;

    @OnClick(R.id.sauce)
    void onSauceClick() {
        if (getString(R.string.url_gitlab).equals(App.get().getAccount().getServerUrl().toString())) {
            Navigator.navigateToProject(AboutActivity.this, REPO_ID);
        } else {
            IntentUtil.openPage(AboutActivity.this, getString(R.string.source_url));
        }
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                if (physicsLayout.getPhysics().getWorld() != null) {
                    gimbal.normalizeGravityEvent(event);
                    physicsLayout.getPhysics().getWorld().setGravity(new Vec2(-event.values[0], event.values[1]));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gimbal = new Gimbal(this);
        gimbal.lock();
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(R.string.about);
        toolbar.setSubtitle(BuildConfig.VERSION_NAME);
        physicsLayout.getPhysics().enableFling();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        App.get().getGitLab().getContributors(REPO_ID)
                .compose(this.<List<Contributor>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<List<Contributor>>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        progress.setVisibility(View.GONE);
                        Snackbar.make(root, R.string.failed_to_load_contributors, Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void success(@NonNull List<Contributor> contributors) {
                        progress.setVisibility(View.GONE);
                        addContributors(Contributor.groupContributors(contributors));
                    }
                });
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, gravitySensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }

    private void addContributors(List<Contributor> contributors) {
        PhysicsConfig config = PhysicsConfig.create();
        config.shapeType = PhysicsConfig.SHAPE_TYPE_CIRCLE;
        int borderSize = getResources().getDimensionPixelSize(R.dimen.border_size);
        int imageSize = getResources().getDimensionPixelSize(R.dimen.circle_size);
        for (int i = 0; i < contributors.size(); i++) {
            Contributor contributor = contributors.get(i);
            CircleImageView imageView = new CircleImageView(this);
            FlowLayout.LayoutParams llp = new FlowLayout.LayoutParams(
                    imageSize,
                    imageSize);
            imageView.setLayoutParams(llp);
            imageView.setBorderWidth(borderSize);
            imageView.setBorderColor(Color.BLACK);
            Physics.setPhysicsConfig(imageView, config);
            physicsLayout.addView(imageView);

            Uri url = ImageUtil.getAvatarUrl(contributor.getEmail(), imageSize);
            App.get().getPicasso()
                    .load(url)
                    .into(imageView);
        }
        physicsLayout.requestLayout();
    }
}
