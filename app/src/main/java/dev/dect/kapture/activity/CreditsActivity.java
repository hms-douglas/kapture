package dev.dect.kapture.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.AppBarLayout;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;

public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);

        setContentView(R.layout.activity_credits);

        initListeners();
    }

    private void initListeners() {
        findViewById(R.id.btnBack).setOnClickListener((v) -> finish());

        ((AppBarLayout) findViewById(R.id.titleBar)).addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            verticalOffset = Math.abs(verticalOffset);

            final float opacity = (float) verticalOffset / ((appBarLayout.getHeight() - findViewById(R.id.toolbar).getHeight()) / 2f);

            findViewById(R.id.titleExpanded).setAlpha(1 - opacity);

            findViewById(R.id.titleCollapsed).setAlpha(opacity != 1 ? opacity - 0.5f : 1);
        });

        findViewById(R.id.btnLicenseApacheV2).setOnClickListener((v) -> openLicenseUrlText(Constants.LICENSE_URL_APACHE_V2));
        findViewById(R.id.btnLicenseLGPLv3).setOnClickListener((v) -> openLicenseUrlText(Constants.LICENSE_URL_LGPL_V3));
        findViewById(R.id.btnLicenseMit).setOnClickListener((v) -> openLicenseUrlText(Constants.LICENSE_URL_MIT));
        findViewById(R.id.btnLicenseLotties).setOnClickListener((v) -> openLicenseUrlText(Constants.LICENSE_URL_LOTTIES));
        findViewById(R.id.btnLicenseBSD3).setOnClickListener((v) -> openLicenseUrlText(Constants.LICENSE_URL_BSD3_CLAUSE));
        findViewById(R.id.btnLicenseOpenFont).setOnClickListener((v) -> openLicenseUrlText(Constants.LICENSE_URL_OPEN_FONT));
    }

    private void openLicenseUrlText(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
