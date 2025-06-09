package dev.dect.kapture.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.core.widget.NestedScrollView;
import androidx.wear.widget.CurvedTextView;

import com.google.android.gms.wearable.Node;
import com.google.android.wearable.input.RotaryEncoderHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.utils.Utils;

public class CreditsActivity extends Activity {

    private CurvedTextView CLOCK_EL;

    private NestedScrollView NESTED_SCROLLER_EL;

    private final Node[] PHONE_NODE = new Node[1];

    private final BroadcastReceiver CLOCK_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateClock();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_credits);

        initVariables();

        initListeners();

        init();
    }

    @Override
    public void onResume() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);

        registerReceiver(CLOCK_RECEIVER, filter);

        updateClock();

        super.onResume();
    }

    @Override
    public void onPause() {
        unregisterReceiver(CLOCK_RECEIVER);

        super.onPause();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        final int scrollTo = (int) (NESTED_SCROLLER_EL.getY() + (RotaryEncoderHelper.getRotaryAxisValue(event) < 0 ? Constants.BEZEL_SCROLL_BY : (Constants.BEZEL_SCROLL_BY * -1)));

        NESTED_SCROLLER_EL.smoothScrollBy(0, scrollTo);

        return true;
    }

    private void initVariables() {
        CLOCK_EL = findViewById(R.id.clock);

        NESTED_SCROLLER_EL = findViewById(R.id.nestedScrollView);

        Utils.Remote.getPhoneNodeAsync(this, PHONE_NODE);
    }

    private void initListeners() {
        findViewById(R.id.btnLicenseApacheV2).setOnClickListener((v) -> openLicenseUrlText(Constants.Url.License.APACHE_V2));
        findViewById(R.id.btnLicenseMit).setOnClickListener((v) -> openLicenseUrlText(Constants.Url.License.MIT));
        findViewById(R.id.btnLicenseBSD3).setOnClickListener((v) -> openLicenseUrlText(Constants.Url.License.BSD3_CLAUSE));
    }

    private void init() {
        updateClock();
    }

    private void updateClock() {
        CLOCK_EL.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
    }

    private void openLicenseUrlText(String url) {
        Utils.Remote.openLinkOnPhone(this, PHONE_NODE, url);
    }
}