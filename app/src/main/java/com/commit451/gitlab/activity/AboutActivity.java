package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.Contributor;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.ImageUtil;
import com.commit451.gitlab.util.IntentUtil;
import com.commit451.gitlab.util.NavigationManager;
import com.commit451.gitlab.util.WindowUtil;
import com.jawnnypoo.physicslayout.Physics;
import com.jawnnypoo.physicslayout.PhysicsConfig;
import com.jawnnypoo.physicslayout.PhysicsFrameLayout;

import org.jbox2d.common.Vec2;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Callback;
import timber.log.Timber;

/**
 * Thats what its all about
 */
public class AboutActivity extends BaseActivity {
    private static final String REPO_ID = "473568";

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        return intent;
    }

    @Bind(R.id.root)
    ViewGroup mRoot;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.contributors)
    TextView mContributors;
    @Bind(R.id.physics_layout)
    PhysicsFrameLayout mPhysicsLayout;
    @Bind(R.id.progress)
    View mProgress;

    @OnClick(R.id.sauce)
    void onSauceClick() {
        if ("https://gitlab.com".equals(GitLabClient.getAccount().getServerUrl().toString())) {
            NavigationManager.navigateToProject(AboutActivity.this, REPO_ID);
        } else {
            IntentUtil.openPage(AboutActivity.this, getString(R.string.source_url));
        }
    }

    SensorManager sensorManager;
    Sensor gravitySensor;

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                if (mPhysicsLayout.getPhysics().getWorld() != null) {
                    WindowUtil.normalizeForOrientation(getWindow(), event);
                    mPhysicsLayout.getPhysics().getWorld().setGravity(new Vec2(-event.values[0], event.values[1]));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private Callback<List<Contributor>> mContributorResponseCallback = new EasyCallback<List<Contributor>>() {
        @Override
        public void onResponse(@NonNull List<Contributor> response) {
            mProgress.setVisibility(View.GONE);
            addContributors(Contributor.groupContributors(response));
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, R.string.failed_to_load_contributors, Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtil.lockToCurrentOrientation(this);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setTitle(R.string.about);
        mPhysicsLayout.getPhysics().enableFling();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        GitLabClient.instance().getContributors(REPO_ID).enqueue(mContributorResponseCallback);
        mProgress.setVisibility(View.VISIBLE);
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
        PhysicsConfig config = new PhysicsConfig.Builder()
                .setShapeType(PhysicsConfig.ShapeType.CIRCLE)
                .setDensity(1.0f)
                .setFriction(0.0f)
                .setRestitution(0.0f)
                .build();
        int x = 0;
        int y = 0;
        int imageSize = getResources().getDimensionPixelSize(R.dimen.circle_size);
        for (int i = 0; i < contributors.size(); i++) {
            Contributor contributor = contributors.get(i);
            ImageView imageView = new ImageView(this);
            FrameLayout.LayoutParams llp = new FrameLayout.LayoutParams(
                    imageSize,
                    imageSize);
            imageView.setLayoutParams(llp);
            Physics.setPhysicsConfig(imageView, config);
            mPhysicsLayout.addView(imageView);
            imageView.setX(x);
            imageView.setY(y);

            x = (x + imageSize);
            if (x > mPhysicsLayout.getWidth()) {
                x = 0;
                y = (y + imageSize) % mPhysicsLayout.getHeight();
            }

            Uri url = ImageUtil.getAvatarUrl(contributor.getEmail(), imageSize);
            GitLabClient.getPicasso()
                    .load(url)
                    .transform(new CircleTransformation())
                    .into(imageView);
        }
        mPhysicsLayout.getPhysics().onLayout(true);
    }
}
