package dev.dect.kapture.activity.profile;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import dev.dect.kapture.R;
import dev.dect.kapture.adapter.ProfileManagerAdapter;
import dev.dect.kapture.utils.KProfile;
import dev.dect.kapture.utils.Utils;

public class ProfileManagerActivity extends AppCompatActivity {
    private RecyclerView RECYCLER_VIEW;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);

        setContentView(R.layout.activity_profile_manager);

        Utils.updateStatusBarColor(this);

        initVariables();

        initListeners();

        init();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        rebuildRecyclerView();
    }

    private void initVariables() {
        RECYCLER_VIEW = findViewById(R.id.recyclerView);
    }

    private void initListeners() {
        findViewById(R.id.btnBack).setOnClickListener((v) -> finish());

        findViewById(R.id.btnNew).setOnClickListener((v) -> KProfile.newProfile(this));

        ((AppBarLayout) findViewById(R.id.titleBar)).addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            verticalOffset = Math.abs(verticalOffset);

            final float opacity = (float) verticalOffset / ((appBarLayout.getHeight() - findViewById(R.id.toolbar).getHeight()) / 2f);

            findViewById(R.id.titleExpanded).setAlpha(1 - opacity);

            findViewById(R.id.titleCollapsed).setAlpha(opacity != 1 ? opacity - 0.5f : 1);
        });

        RECYCLER_VIEW.setOnClickListener((v) -> rebuildRecyclerView());
    }

    private void init() {
        RECYCLER_VIEW.setNestedScrollingEnabled(false);
        RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(this));

        rebuildRecyclerView();
    }

    private void rebuildRecyclerView() {
        if(KProfile.getAllProfilesName(this).length() == 0) {
            Toast.makeText(this, getString(R.string.toast_error_no_profiles_found), Toast.LENGTH_SHORT).show();

            finish();
        } else {
            RECYCLER_VIEW.removeAllViews();

            RECYCLER_VIEW.setAdapter(new ProfileManagerAdapter(this));
        }
    }
}
