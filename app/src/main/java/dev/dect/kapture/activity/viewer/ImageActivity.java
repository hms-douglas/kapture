package dev.dect.kapture.activity.viewer;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Objects;

import dev.dect.kapture.R;

@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class ImageActivity extends AppCompatActivity {
    public static final String INTENT_URL = "url";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_viewer_image);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        init();
    }

    private void init() {
        final File f = new File(Objects.requireNonNull(getIntent().getStringExtra(INTENT_URL)));

        if(!f.exists()) {
            Toast.makeText(this, getString(R.string.toast_error_file_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final ActivityManager.TaskDescription.Builder taskDescription = new ActivityManager.TaskDescription.Builder();

        taskDescription.setLabel(f.getName());

        setTaskDescription(taskDescription.build());

        ((ImageView) findViewById(R.id.viewImage)).setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));
    }
}
