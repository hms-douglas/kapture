package dev.dect.kapture.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.adapter.FilePickerFolderAdapter;
import dev.dect.kapture.adapter.FilePickerImageAdapter;
import dev.dect.kapture.adapter.FilePickerPathAdapter;
import dev.dect.kapture.adapter.FilePickerStorageAdapter;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.popup.InputPopup;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.Utils;

public class FilePickerActivity extends AppCompatActivity {
    public static final String INTENT_PATH = "path",
                               INTENT_TYPE = "type",
                               TYPE_FOLDER = "folder",
                               TYPE_IMAGE = "image";

    private TextView CURRENT_FOLDER_NAME_EL,
                     TITLE_COLLAPSED_EL,
                     EMPTY_EL;

    private File CURRENT_FOLDER;

    private RecyclerView RECYCLER_VIEW,
                         PATH_RECYCLER_VIEW;

    private String PICKING_TYPE;

    private ArrayList<File> STORAGES_ROOT;

    private ImageButton BTN_CREATE;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);

        setContentView(R.layout.activity_file_picker);

        Utils.updateStatusBarColor(this);

        initVariables();

        initListeners();

        init();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    private void initVariables() {
        final String path = getIntent().hasExtra(INTENT_PATH) ? getIntent().getExtras().getString(INTENT_PATH) : null;

        if(path != null && !path.isEmpty()) {
            final File f = new File(path);

            if(f.exists()) {
                if(f.isDirectory()) {
                    CURRENT_FOLDER = f;
                } else {
                    CURRENT_FOLDER = f.getParentFile();
                }
            }
        }

        PICKING_TYPE = getIntent().getStringExtra(INTENT_TYPE);

        CURRENT_FOLDER_NAME_EL = findViewById(R.id.titleExpanded);
        TITLE_COLLAPSED_EL = findViewById(R.id.titleCollapsed);
        EMPTY_EL = findViewById(R.id.emptyFolder);

        RECYCLER_VIEW = findViewById(R.id.recycler);

        PATH_RECYCLER_VIEW = findViewById(R.id.recyclerPath);

        BTN_CREATE = findViewById(R.id.btnCreateFolder);

        initStorageVariable();
    }

    private void initListeners() {
        ((AppBarLayout) findViewById(R.id.titleBar)).addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            verticalOffset = Math.abs(verticalOffset);

            final float opacity = (float) verticalOffset / ((appBarLayout.getHeight() - findViewById(R.id.toolbar).getHeight()) / 2f);

            findViewById(R.id.titleExpanded).setAlpha(1 - opacity);

            findViewById(R.id.titleCollapsed).setAlpha(opacity != 1 ? opacity - 0.5f : 1);
        });

        findViewById(R.id.btnSelect).setOnClickListener((v) -> {
            if(CURRENT_FOLDER != null) {
                final Intent i = new Intent();

                i.putExtra(INTENT_PATH, CURRENT_FOLDER.getAbsolutePath());

                setResult(RESULT_OK, i);
            }

            finish();
        });

        findViewById(R.id.btnCancel).setOnClickListener((v) -> finish());

        findViewById(R.id.btnBack).setOnClickListener((v) -> goBack());

        findViewById(R.id.btnHome).setOnClickListener((v) -> {
            if(STORAGES_ROOT.size() > 1) {
                goHome();
            } else {
                loadFolder(Environment.getExternalStorageDirectory());
            }
        });

        findViewById(R.id.btnCreateFolder).setOnClickListener((v) -> {
            new InputPopup.Text(
                    this,
                    R.string.popup_title_create_folder,
                    "",
                    R.string.popup_btn_create,
                    new InputPopup.OnInputPopupListener() {
                        @Override
                        public void onStringInputSet(String input) {
                            File newFolder = new File(CURRENT_FOLDER, input);

                            if(newFolder.exists()) {
                                Toast.makeText(FilePickerActivity.this, getString(R.string.toast_error_folder_already_exists), Toast.LENGTH_SHORT).show();
                            } else if(newFolder.mkdirs()) {
                                loadFolder(newFolder);

                                Toast.makeText(FilePickerActivity.this, getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(FilePickerActivity.this, getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    R.string.popup_btn_cancel,
                    null,
                    true,
                    false,
                    false
            ).show();
        });
    }

    private void init() {
        initFolderRecyclerView();

        initPathRecyclerView();

        setInterface();

        if(CURRENT_FOLDER == null) {
            if(STORAGES_ROOT.size() > 1) {
                goHome();
            } else {
                loadFolder(Environment.getExternalStorageDirectory());
            }
        } else {
            loadFolder(CURRENT_FOLDER);
        }
    }

    private void initStorageVariable() {
        STORAGES_ROOT = new ArrayList<>();

        STORAGES_ROOT.add(Environment.getExternalStorageDirectory());

        final File dualMessenger = new File(Constants.DUAL_MESSENGER_STORAGE_PATH);

        if(dualMessenger.exists()) {
            STORAGES_ROOT.add(dualMessenger);
        }

        for(StorageVolume s : ((StorageManager) this.getSystemService(STORAGE_SERVICE)).getStorageVolumes()) {
            if(!s.getState().equals(Environment.MEDIA_MOUNTED) || Objects.requireNonNull(s.getDirectory()).getAbsolutePath().contains(Constants.INTERNAL_STORAGE_PATH)) {
                continue;
            }

            STORAGES_ROOT.add(s.getDirectory());
        }
    }

    private void initFolderRecyclerView() {
        RECYCLER_VIEW.setNestedScrollingEnabled(false);
        RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initPathRecyclerView() {
        PATH_RECYCLER_VIEW.setNestedScrollingEnabled(false);
        PATH_RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setInterface() {
        if(PICKING_TYPE.equals(TYPE_FOLDER)) {
            TITLE_COLLAPSED_EL.setText(R.string.pick_file_select_folder);
            BTN_CREATE.setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.btnSelect).setVisibility(View.GONE);

            BTN_CREATE.setVisibility(View.GONE);

            if(Objects.requireNonNull(getIntent().getStringExtra(INTENT_TYPE)).equals(TYPE_IMAGE)) {
                TITLE_COLLAPSED_EL.setText(R.string.pick_file_select_an_image);
            }
        }
    }

    private void loadFolder(File fs) {
        CURRENT_FOLDER = fs;

        if(isRootDirectory(CURRENT_FOLDER)) {
            switch(CURRENT_FOLDER.getName()) {
                case "0":
                    CURRENT_FOLDER_NAME_EL.setText(R.string.internal_storage);
                    break;

                case "95":
                    CURRENT_FOLDER_NAME_EL.setText(R.string.dual_storage);
                    break;

                default:
                    CURRENT_FOLDER_NAME_EL.setText(R.string.sd_card);
                    break;
            }
        } else {
            CURRENT_FOLDER_NAME_EL.setText(CURRENT_FOLDER.getName());
        }

        final ConcatAdapter concatAdapter = new ConcatAdapter();

        boolean isEmpty = true;

        final ArrayList<File> folders = new ArrayList<>(),
                              files = new ArrayList<>();

        for(File f : Objects.requireNonNull(fs.listFiles())) {
            if(f.isDirectory()) {
                folders.add(f);
            } else {
                files.add(f);
            }
        }

        if(!folders.isEmpty()) {
            concatAdapter.addAdapter(new FilePickerFolderAdapter(folders, this::loadFolder));
            isEmpty = false;
        }

        final ArrayList<File> images = new ArrayList<>();

        if(PICKING_TYPE.equals(TYPE_IMAGE)) {
            for(File f : files) {
                try {
                    if(MimeTypeMap.getSingleton().getMimeTypeFromExtension(KFile.getFileExtension(f)).contains("image/")) {
                        images.add(f);
                    }
                } catch (Exception ignore) {}
            }

            if(!images.isEmpty()) {
                concatAdapter.addAdapter(new FilePickerImageAdapter(images, this::pickImage));
                isEmpty = false;
            }
        }

        RECYCLER_VIEW.removeAllViews();

        RECYCLER_VIEW.setAdapter(concatAdapter);

        EMPTY_EL.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        loadPath(fs);
    }

    private void loadPath(File folder) {
        PATH_RECYCLER_VIEW.setAdapter(new FilePickerPathAdapter(folder, STORAGES_ROOT, this::loadFolder));

        PATH_RECYCLER_VIEW.scrollToPosition(Objects.requireNonNull(PATH_RECYCLER_VIEW.getAdapter()).getItemCount() - 1);
    }

    private void pickImage(File f) {
        final Intent i = new Intent();

        i.putExtra(INTENT_PATH, f.getAbsolutePath());

        setResult(RESULT_OK, i);

        finish();
    }

    private void goBack() {
        if(CURRENT_FOLDER == null) {
            finish();
        } else if(isRootDirectory(CURRENT_FOLDER)) {
            if(STORAGES_ROOT.size() > 1) {
                goHome();
            } else {
                finish();
            }
        } else {
            loadFolder(CURRENT_FOLDER.getParentFile());
        }
    }

    private void goHome() {
        CURRENT_FOLDER = null;

        EMPTY_EL.setVisibility(View.GONE);
        BTN_CREATE.setVisibility(View.GONE);
        findViewById(R.id.btnsContainer).setVisibility(View.GONE);
        findViewById(R.id.toolbarExtra).setVisibility(View.GONE);

        TITLE_COLLAPSED_EL.setText(R.string.pick_file_select_storage);
        CURRENT_FOLDER_NAME_EL.setText(R.string.pick_file_select_storage);

        RECYCLER_VIEW.removeAllViews();

        RECYCLER_VIEW.setAdapter(new FilePickerStorageAdapter(STORAGES_ROOT, this::pickStorage));
    }

    private void pickStorage(File root) {
        CURRENT_FOLDER = root;

        findViewById(R.id.btnsContainer).setVisibility(View.VISIBLE);

        findViewById(R.id.toolbarExtra).setVisibility(View.VISIBLE);

        setInterface();

        loadFolder(root);
    }

    private boolean isRootDirectory(File folder) {
        for(File f: STORAGES_ROOT) {
            if(f.getAbsolutePath().equals(folder.getAbsolutePath())) {
                return true;
            }
        }

        return folder.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath());
    }
}
