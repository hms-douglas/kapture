package dev.dect.kapture.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.AboutActivity;
import dev.dect.kapture.adapter.KaptureAdapter;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DB;
import dev.dect.kapture.data.DefaultSettings;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.popup.DialogPopup;
import dev.dect.kapture.popup.ExtraPopup;
import dev.dect.kapture.popup.InputPopup;
import dev.dect.kapture.popup.ScreenshotPopup;
import dev.dect.kapture.popup.SortPopup;
import dev.dect.kapture.server.WifiShare;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.KProfile;
import dev.dect.kapture.utils.Utils;

/** @noinspection ResultOfMethodCallIgnored*/
@SuppressLint({"ApplySharedPref", "SetTextI18n"})
public class KapturesFragment extends Fragment {
    public interface OnCaptureFragment {
        void onSelection(boolean hasSelection, int amountSelected);
    }

    public static final int STYLE_LIST = 0,
                            STYLE_GRID_BIG = 1,
                            STYLE_GRID_SMALL = 2;

    private Context CONTEXT;

    private View VIEW;

    private RecyclerView RECYCLER_VIEW;

    private AppCompatButton BTN_FLOATING_START_STOP,
                            BTN_FLOATING_PAUSE_RESUME,
                            BTN_PROFILE;

    private ImageButton BTN_BACK_TO_TOP;

    private LinearLayout BTNS_FLOATING_CONTAINER;

    private NestedScrollView NESTED_SCROLL_VIEW;

    private ArrayList<Kapture> KAPTURES;

    private KaptureAdapter ADAPTER;

    private DB DATABASE;

    private SwipeRefreshLayout SWIPE_REFRESH;

    private int SORT_BY;

    private boolean SORT_ASC,
                    IS_TABLET_UI;

    private SelectionTracker<Long> TRACKER;

    private AppCompatButton BTN_SELECTED;

    private ImageButton BTN_STYLE_LAYOUT_MANAGER,
                        BTN_SETTINGS__TABLET_UI;

    private OnCaptureFragment LISTENER;

    private SharedPreferences SP_APP;

    private ConstraintLayout SEARCH_CONTAINER;

    private EditText SEARCH_INPUT;

    private ImageButton SEARCH_BTN_MIC_OR_CLEAR;

    private ActivityResultLauncher<Intent> LAUNCH_ACTIVITY_RESULT_FOR_SPEECH_TO_TEXT;

    private TextView SUBTITLE;

    private AppBarLayout TITLE_BAR;

    private SettingsFragment SETTINGS_FRAGMENT__TABLE_UI;

    private Handler SCROLL_HANDLER;

    private SharedPreferences.OnSharedPreferenceChangeListener PROFILE_LISTENER;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        VIEW = inflater.inflate(R.layout.fragment_kaptures, container, false);

        initVariables();

        initListeners();

        init();

        return VIEW;
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshAll();

        KProfile.updateButtonUi(CONTEXT, BTN_PROFILE);
    }

    @Override
    public void onDestroy() {
        try {
            SP_APP.unregisterOnSharedPreferenceChangeListener(PROFILE_LISTENER);
        } catch (Exception ignore) {}

        super.onDestroy();
    }

    public void setListener(OnCaptureFragment listener) {
        this.LISTENER = listener;
    }

    private void initVariables() {
        CONTEXT = VIEW.getContext();

        TITLE_BAR = VIEW.findViewById(R.id.titleBar);

        NESTED_SCROLL_VIEW = VIEW.findViewById(R.id.nestedScrollView);

        RECYCLER_VIEW = VIEW.findViewById(R.id.recyclerView);

        SWIPE_REFRESH = VIEW.findViewById(R.id.swipeToRefresh);

        BTN_SELECTED = VIEW.findViewById(R.id.selected);
        BTN_STYLE_LAYOUT_MANAGER = VIEW.findViewById(R.id.btnGrid);

        BTN_FLOATING_START_STOP = VIEW.findViewById(R.id.startStopCapturing);
        BTN_FLOATING_PAUSE_RESUME = VIEW.findViewById(R.id.pauseResumeCapturing);

        BTN_PROFILE = VIEW.findViewById(R.id.btnProfile);

        BTN_BACK_TO_TOP = VIEW.findViewById(R.id.btnBackToTop);

        BTNS_FLOATING_CONTAINER = VIEW.findViewById(R.id.controlsBtnContainer);

        SP_APP = KSharedPreferences.getAppSp(CONTEXT);

        SORT_BY = SP_APP.getInt(Constants.Sp.App.SORT_BY, DefaultSettings.SORT_BY);
        SORT_ASC = SP_APP.getBoolean(Constants.Sp.App.SORT_BY_ASC, DefaultSettings.SORT_BY_ASC);

        SEARCH_CONTAINER = VIEW.findViewById(R.id.searchContainer);
        SEARCH_INPUT = VIEW.findViewById(R.id.searchInput);
        SEARCH_BTN_MIC_OR_CLEAR = VIEW.findViewById(R.id.btnMicClear);

        SUBTITLE = VIEW.findViewById(R.id.subtitleExpanded);

        SCROLL_HANDLER = new Handler(Looper.getMainLooper());

        LAUNCH_ACTIVITY_RESULT_FOR_SPEECH_TO_TEXT = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<String> r = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    SEARCH_INPUT.setText(Objects.requireNonNull(r).get(0));

                    SEARCH_BTN_MIC_OR_CLEAR.setImageResource(R.drawable.icon_tool_bar_clear);
                }
            }
        );

        PROFILE_LISTENER = KProfile.createAndAddProfileListenerUpdate(SP_APP, BTN_PROFILE, null);

        DATABASE = new DB(CONTEXT);

        getAndValidateKaptures();

        IS_TABLET_UI = CONTEXT.getResources().getBoolean(R.bool.is_tablet);

        if(IS_TABLET_UI) {
            BTN_SETTINGS__TABLET_UI = VIEW.findViewById(R.id.btnSettings);

            SETTINGS_FRAGMENT__TABLE_UI = new SettingsFragment();
        }
    }

    private void initListeners() {
        SWIPE_REFRESH.setOnRefreshListener(this::triggerRefresh);

        VIEW.findViewById(R.id.btnMore).setOnClickListener((v) -> {
            final PopupMenu popupMenu = new PopupMenu(CONTEXT, v, Gravity.END, 0, R.style.KPopupMenu);

            final Menu menu = popupMenu.getMenu();

            menu.setGroupDividerEnabled(true);

            popupMenu.getMenuInflater().inflate(R.menu.capture_more, menu);

            if(KAPTURES.size() < 2) {
                menu.findItem(R.id.menuSort).setEnabled(false);

                if(KAPTURES.isEmpty()) {
                    menu.findItem(R.id.menuSelectAll).setEnabled(false);
                    menu.findItem(R.id.wifiShare).setEnabled(false);
                }
            }

            popupMenu.setOnMenuItemClickListener((item) -> {
                final int id = item.getItemId();

                if(id == R.id.menuRefresh) {
                    triggerRefresh();
                } else if(id == R.id.menuSelectAll) {
                    selectAll();
                } else if(id == R.id.menuSort) {
                    new SortPopup(CONTEXT, SORT_BY, SORT_ASC, this::sortBy).show();
                } else if(id == R.id.wifiShare) {
                    new WifiShare(CONTEXT, KAPTURES).start();
                } else if(id == R.id.menuOpenAccessibility) {
                    Utils.ExternalActivity.requestAccessibility(CONTEXT);
                } else if(id == R.id.menuShowCommand) {
                    new DialogPopup(
                        CONTEXT,
                        DialogPopup.NO_TEXT,
                        R.string.command,
                        R.string.popup_btn_ok,
                        null,
                        DialogPopup.NO_TEXT,
                        null,
                        true,
                        false,
                        false
                    ).show();
                } else if(id == R.id.menuAbout) {
                    startActivity(new Intent(CONTEXT, AboutActivity.class));
                }

                return true;
            });

            popupMenu.show();
        });

        BTN_FLOATING_START_STOP.setOnClickListener((l) -> CapturingService.requestToggleRecording(CONTEXT));

        BTN_FLOATING_PAUSE_RESUME.setOnClickListener((l) -> CapturingService.requestTogglePauseResumeRecording());

        BTN_SELECTED.setOnClickListener((l) -> {
            if(TRACKER.getSelection().size() == ADAPTER.getItemCount()) {
                unselectAll();
            } else {
                selectAll();
            }
        });

        BTN_STYLE_LAYOUT_MANAGER.setOnClickListener((v) -> {
            switch(SP_APP.getInt(Constants.Sp.App.LAYOUT_MANAGER_STYLE, DefaultSettings.LAYOUT_MANAGER_STYLE)) {
                case STYLE_GRID_BIG:
                    setLayoutManager(STYLE_LIST);
                    break;

                case STYLE_GRID_SMALL:
                    setLayoutManager(STYLE_GRID_BIG);
                    break;

                default:
                    setLayoutManager(STYLE_GRID_SMALL);
                    break;
            }
        });

        SEARCH_INPUT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().isEmpty()) {
                    SEARCH_BTN_MIC_OR_CLEAR.setImageResource(R.drawable.icon_tool_bar_microphone);
                } else if(s.toString().trim().length() == 1) {
                    SEARCH_BTN_MIC_OR_CLEAR.setImageResource(R.drawable.icon_tool_bar_clear);
                }

                ADAPTER.getFilter().filter(s, (i) -> VIEW.findViewById(R.id.noCapture).setVisibility(ADAPTER.getItemCount() == 0 ? View.VISIBLE : View.GONE));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        SEARCH_BTN_MIC_OR_CLEAR.setOnClickListener((v) -> {
            if(SEARCH_INPUT.getText().toString().trim().isEmpty()) {
                final Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                LAUNCH_ACTIVITY_RESULT_FOR_SPEECH_TO_TEXT.launch(i);
            } else {
                clearSearch();
            }
        });

        VIEW.findViewById(R.id.btnSearch).setOnClickListener((v) -> toggleSearch());

        VIEW.findViewById(R.id.btnBack).setOnClickListener((v) -> closeSearch());

        BTN_BACK_TO_TOP.setOnClickListener((v) -> NESTED_SCROLL_VIEW.smoothScrollTo(0, 0));

        NESTED_SCROLL_VIEW.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, x, y, oldX, oldY) -> {
            SCROLL_HANDLER.removeCallbacksAndMessages(null);

            if(y <= 5) {
                BTN_BACK_TO_TOP.setVisibility(View.GONE);
                BTNS_FLOATING_CONTAINER.setVisibility(View.VISIBLE);
            } else {
                BTN_BACK_TO_TOP.setVisibility(View.VISIBLE);
                BTNS_FLOATING_CONTAINER.setVisibility(View.GONE);

                SCROLL_HANDLER.postDelayed(() -> {
                    BTNS_FLOATING_CONTAINER.setVisibility(View.VISIBLE);
                    BTN_BACK_TO_TOP.setVisibility(View.GONE);
                }, 1500);
            }
        });

        if(IS_TABLET_UI) {
            BTN_SETTINGS__TABLET_UI.setOnClickListener((v) -> {
                if(SETTINGS_FRAGMENT__TABLE_UI.isVisible()) {
                    getChildFragmentManager().beginTransaction().hide(SETTINGS_FRAGMENT__TABLE_UI).commitNow();
                    VIEW.findViewById(R.id.frameLayoutContainer).setVisibility(View.GONE);
                } else {
                    openSettings_tabletUi();
                }
            });

            VIEW.findViewById(R.id.frameLayoutContainer).setOnClickListener((v) -> BTN_SETTINGS__TABLET_UI.callOnClick());

            getActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if(SETTINGS_FRAGMENT__TABLE_UI.isVisible()) {
                        BTN_SETTINGS__TABLET_UI.callOnClick();
                    } else {
                        getActivity().moveTaskToBack(false);
                    }
                }
            });

            TITLE_BAR.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                verticalOffset = Math.abs(verticalOffset);

                final float opacity = (float) verticalOffset / ((appBarLayout.getHeight() - VIEW.findViewById(R.id.toolbar).getHeight()) / 2f);

                VIEW.findViewById(R.id.titleAndSubtitle).setAlpha(1 - opacity);
            });
        } else {
            TITLE_BAR.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                verticalOffset = Math.abs(verticalOffset);

                final float opacity = (float) verticalOffset / ((appBarLayout.getHeight() - VIEW.findViewById(R.id.toolbar).getHeight()) / 2f);

                VIEW.findViewById(R.id.titleAndSubtitle).setAlpha(1 - opacity);
            });
        }
    }

    private void toggleSearch() {
        if(isSearching()) {
            closeSearch();
        } else {
            openSearch();
        }
    }

    private void openSearch() {
        if(IS_TABLET_UI) {
            BTN_PROFILE.setVisibility(View.GONE);
        }

        SEARCH_CONTAINER.setVisibility(View.VISIBLE);

        TITLE_BAR.setExpanded(false, true);

        Utils.Keyboard.requestShowForInput(SEARCH_INPUT);
    }

    public void closeSearch() {
        if(IS_TABLET_UI) {
            BTN_PROFILE.setVisibility(View.VISIBLE);
        }

        Utils.Keyboard.requestCloseFromInput(CONTEXT, SEARCH_INPUT);

        SEARCH_CONTAINER.setVisibility(View.GONE);

        VIEW.findViewById(R.id.noCapture).setVisibility(View.GONE);

        clearSearch();
    }

    private void clearSearch() {
        SEARCH_INPUT.setText("");

        SEARCH_BTN_MIC_OR_CLEAR.setImageResource(R.drawable.icon_tool_bar_microphone);
    }

    public boolean isSearching() {
        return SEARCH_CONTAINER.getVisibility() == View.VISIBLE;
    }

    private void init() {
        buildRecyclerView();

        requestFloatingButtonUpdate();

        KProfile.init(BTN_PROFILE);

        if(IS_TABLET_UI) {
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

            fragmentTransaction.replace(R.id.frameLayout, SETTINGS_FRAGMENT__TABLE_UI);

            fragmentTransaction.hide(SETTINGS_FRAGMENT__TABLE_UI);

            fragmentTransaction.commitNow();
        }
    }

    private void getAndValidateKaptures() {
        KAPTURES = new ArrayList<>();

        for(Kapture kapture : DATABASE.selectAllKaptures(true)) {
            if(new File(kapture.getLocation()).exists()) {
                if(kapture.hasExtras()) {
                    final ArrayList<Kapture.Extra> extras = new ArrayList<>();

                    for(Kapture.Extra extra : kapture.getExtras()) {
                        if(new File(extra.getLocation()).exists()) {
                            extras.add(extra);
                        } else {
                            DATABASE.deleteExtra(extra);
                        }
                    }

                    if(extras.size() != kapture.getExtras().size()) {
                        kapture.setExtras(extras);
                    }
                }

                if(kapture.hasScreenshots()) {
                    final ArrayList<Kapture.Screenshot> screenshots = new ArrayList<>();

                    for(Kapture.Screenshot screenshot : kapture.getScreenshots()) {
                        if(new File(screenshot.getLocation()).exists()) {
                            screenshots.add(screenshot);
                        } else {
                            DATABASE.deleteScreenshot(screenshot);
                        }
                    }

                    if(screenshots.size() != kapture.getExtras().size()) {
                        kapture.setScreenshots(screenshots);
                    }
                }

                KAPTURES.add(kapture);
            } else {
                if(kapture.getThumbnailCachedFile().exists()) {
                    kapture.getThumbnailCachedFile().delete();
                }

                DATABASE.deleteKapture(kapture);
            }
        }

        sortAdapter(false);
    }

    private void buildRecyclerView() {
        ADAPTER = new KaptureAdapter(KAPTURES);

        RECYCLER_VIEW.setNestedScrollingEnabled(false);

        setLayoutManager(SP_APP.getInt(Constants.Sp.App.LAYOUT_MANAGER_STYLE, DefaultSettings.LAYOUT_MANAGER_STYLE));

        if(KAPTURES.isEmpty()) {
            VIEW.findViewById(R.id.noCapture).setVisibility(View.VISIBLE);
            RECYCLER_VIEW.setVisibility(View.GONE);
        } else {
            RECYCLER_VIEW.setVisibility(View.VISIBLE);
        }

        initTracker();
    }

    private void setLayoutManager(int style) {
        if(IS_TABLET_UI) {
            switch (style) {
                case STYLE_GRID_BIG:
                    RECYCLER_VIEW.setLayoutManager(new GridLayoutManager(CONTEXT, 2));
                    BTN_STYLE_LAYOUT_MANAGER.setImageResource(R.drawable.icon_tool_bar_grid_list);
                    break;

                case STYLE_GRID_SMALL:
                    RECYCLER_VIEW.setLayoutManager(new GridLayoutManager(CONTEXT, 4));
                    BTN_STYLE_LAYOUT_MANAGER.setImageResource(R.drawable.icon_tool_bar_grid_small);
                    break;

                default:
                    RECYCLER_VIEW.setLayoutManager(new GridLayoutManager(CONTEXT, 2));
                    BTN_STYLE_LAYOUT_MANAGER.setImageResource(R.drawable.icon_tool_bar_grid_big);
                    break;
            }
        } else {
            switch (style) {
                case STYLE_GRID_BIG:
                    RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(CONTEXT));
                    BTN_STYLE_LAYOUT_MANAGER.setImageResource(R.drawable.icon_tool_bar_grid_list);
                    break;

                case STYLE_GRID_SMALL:
                    RECYCLER_VIEW.setLayoutManager(new GridLayoutManager(CONTEXT, 2));
                    BTN_STYLE_LAYOUT_MANAGER.setImageResource(R.drawable.icon_tool_bar_grid_small);
                    break;

                default:
                    RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(CONTEXT));
                    BTN_STYLE_LAYOUT_MANAGER.setImageResource(R.drawable.icon_tool_bar_grid_big);
                    break;
            }
        }

        RECYCLER_VIEW.setAdapter(ADAPTER);

        SP_APP.edit().putInt(Constants.Sp.App.LAYOUT_MANAGER_STYLE, style).commit();
    }

    private void setEmptyAdapterIfEmpty() {
        if(KAPTURES.isEmpty()) {
            RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(CONTEXT));

            RECYCLER_VIEW.removeAllViews();

            VIEW.findViewById(R.id.noCapture).setVisibility(View.VISIBLE);
            RECYCLER_VIEW.setVisibility(View.GONE);

            BTN_STYLE_LAYOUT_MANAGER.setVisibility(View.GONE);

            VIEW.findViewById(R.id.btnSearch).setVisibility(View.GONE);
        } else if(KAPTURES.size() == 1) {
            VIEW.findViewById(R.id.noCapture).setVisibility(View.GONE);
            RECYCLER_VIEW.setVisibility(View.VISIBLE);

            BTN_STYLE_LAYOUT_MANAGER.setVisibility(View.VISIBLE);

            setLayoutManager(SP_APP.getInt(Constants.Sp.App.LAYOUT_MANAGER_STYLE, DefaultSettings.LAYOUT_MANAGER_STYLE));

            VIEW.findViewById(R.id.btnSearch).setVisibility(View.VISIBLE);
        }
    }

    private void initTracker() {
        TRACKER = new SelectionTracker.Builder<>(
            "kaptures",
            RECYCLER_VIEW,
            new StableIdKeyProvider(RECYCLER_VIEW),
            new KaptureAdapter.ItemLookup(RECYCLER_VIEW),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build();

        ADAPTER.setTracker(TRACKER);

        TRACKER.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                final int size = TRACKER.getSelection().size();

                if(size > 0) {
                    Utils.Keyboard.requestCloseFromInput(CONTEXT, SEARCH_INPUT);

                    BTN_SELECTED.setText(size + " " + (size == 1 ? CONTEXT.getString(R.string.selected) : CONTEXT.getString(R.string.selected_plural)));

                    BTN_SELECTED.setCompoundDrawablesWithIntrinsicBounds(
                        Utils.getDrawable(CONTEXT, size == ADAPTER.getItemCount() ? R.drawable.checkbox_on : R.drawable.checkbox_off),
                        null,
                        null,
                        null
                    );

                    VIEW.findViewById(R.id.selectedContainer).setVisibility(View.VISIBLE);
                } else {
                    VIEW.findViewById(R.id.selectedContainer).setVisibility(View.GONE);
                }

                if(LISTENER != null) {
                    LISTENER.onSelection(TRACKER.hasSelection(), size);
                }
            }
        });
    }

    public void triggerRefresh() {
        SWIPE_REFRESH.setRefreshing(true);

        new CountDownTimer(1500, 500) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                refreshAll();

                SWIPE_REFRESH.setRefreshing(false);

                Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    private void refreshAll() {
        final ArrayList<Integer> indexesHelper = new ArrayList<>();

        for(int i = 0; i < KAPTURES.size(); i++) {
            if(!new File(KAPTURES.get(i).getLocation()).exists()) {
                final Kapture kapture = KAPTURES.get(i);

                if(kapture.getThumbnailCachedFile().exists()) {
                    kapture.getThumbnailCachedFile().delete();
                }

                DATABASE.deleteKapture(kapture);

                indexesHelper.add(i);
            }
        }

        for(int i = indexesHelper.size() - 1; i >= 0; i--) {
            final int indexToRemove = indexesHelper.get(i);

            KAPTURES.remove(indexToRemove);
            ADAPTER.notifyItemRemoved(indexToRemove);
        }

        indexesHelper.clear();

        for(int i = 0; i < KAPTURES.size(); i++) {
            final Kapture kapture = KAPTURES.get(i);

            if(kapture.hasExtras()) {
                final ArrayList<Kapture.Extra> extras = new ArrayList<>();

                for(Kapture.Extra extra : kapture.getExtras()) {

                    if(new File(extra.getLocation()).exists()) {
                        extras.add(extra);
                    } else {

                        DATABASE.deleteExtra(extra);
                    }
                }

                if(extras.size() != kapture.getExtras().size()) {
                    kapture.setExtras(extras);

                    indexesHelper.add(i);
                }
            }

            if(kapture.hasScreenshots()) {
                final ArrayList<Kapture.Screenshot> screenshots = new ArrayList<>();

                for(Kapture.Screenshot screenshot : kapture.getScreenshots()) {

                    if(new File(screenshot.getLocation()).exists()) {
                        screenshots.add(screenshot);
                    } else {

                        DATABASE.deleteScreenshot(screenshot);
                    }
                }

                if(screenshots.size() != kapture.getScreenshots().size()) {
                    kapture.setScreenshots(screenshots);

                    indexesHelper.add(i);
                }
            }
        }

        for(int i = 0; i < indexesHelper.size(); i++) {
            final int indexToUpdate = indexesHelper.get(i);

            ADAPTER.notifyItemChanged(indexToUpdate);
        }

        setEmptyAdapterIfEmpty();

        updateSubtitle();
    }

    private void sortBy(int sortBy, boolean asc) {
        SORT_BY = sortBy;
        SORT_ASC = asc;

        SP_APP.edit()
            .putInt(Constants.Sp.App.SORT_BY, SORT_BY)
            .putBoolean(Constants.Sp.App.SORT_BY_ASC, SORT_ASC)
        .apply();

        sortAdapter(true);
    }

    private void sortAdapter(boolean notify) {
        sortList(KAPTURES);

        if(notify) {
            clearSearch();

            updateSubtitle();
        }
    }

    private void sortList(ArrayList<Kapture> list) {
        if(SORT_ASC) {
            switch(SORT_BY) {
                case SortPopup.SORT_NAME:
                    list.sort(Comparator.comparing(Kapture::getName));
                    break;

                case SortPopup.SORT_DATE_CAPTURING:
                    list.sort(Comparator.comparing(Kapture::getCreationTime));
                    break;

                case SortPopup.SORT_SIZE:
                    list.sort(Comparator.comparing(Kapture::getSize));
                    break;

                case SortPopup.SORT_DURATION:
                    list.sort(Comparator.comparing(Kapture::getDuration));
                    break;

            }
        } else {
            switch(SORT_BY) {
                case SortPopup.SORT_NAME:
                    list.sort(Comparator.comparing(Kapture::getName).reversed());
                    break;

                case SortPopup.SORT_DATE_CAPTURING:
                    list.sort(Comparator.comparing(Kapture::getCreationTime).reversed());
                    break;

                case SortPopup.SORT_SIZE:
                    list.sort(Comparator.comparing(Kapture::getSize).reversed());
                    break;

                case SortPopup.SORT_DURATION:
                    list.sort(Comparator.comparing(Kapture::getDuration).reversed());
                    break;
            }
        }
    }

    private void selectAll() {
        for(int i = 0; i < ADAPTER.getItemCount(); i++) {
            TRACKER.select((long) i);
        }
    }

    public void unselectAll() {
        if(TRACKER.hasSelection()) {
            for (int i = 0; i < ADAPTER.getItemCount(); i++) {
                TRACKER.deselect((long) i);
            }
        }
    }

    public boolean isSelection() {
        return TRACKER.hasSelection();
    }

    private ArrayList<Kapture> getSelected() {
        final ArrayList<Kapture> kaptures = new ArrayList<>();

        for(long index : TRACKER.getSelection()) {
            kaptures.add(KAPTURES.get((int) index));
        }

        return kaptures;
    }

    public void deleteSelected() {
        if(!TRACKER.hasSelection()) {
            return;
        }

        new DialogPopup(
            CONTEXT,
            DialogPopup.NO_TEXT,
            getString(R.string.popup_delete_text_2),
            R.string.popup_btn_delete,
            () -> {
                final ArrayList<Kapture> toRemove = getSelected();

                for(Kapture kapture : toRemove) {
                    File f = new File(kapture.getLocation());

                    if(f.exists()) {
                        f.delete();
                    }

                    for(Kapture.Extra extra : kapture.getExtras()) {
                        f = new File(extra.getLocation());

                        if(f.exists()) {
                            f.delete();
                        }
                    }

                    for(Kapture.Screenshot screenshot : kapture.getScreenshots()) {
                        f = new File(screenshot.getLocation());

                        if(f.exists()) {
                            f.delete();
                        }
                    }

                    if(kapture.getThumbnailCachedFile().exists()) {
                        kapture.getThumbnailCachedFile().delete();
                    }

                    DATABASE.deleteKapture(kapture);
                }

                unselectAll();

                KAPTURES.removeAll(toRemove);

                clearSearch();

                setEmptyAdapterIfEmpty();

                updateSubtitle();

                Toast.makeText(CONTEXT, getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();
            },
            R.string.popup_btn_cancel,
            null,
            false,
            false,
            true
        ).show();
    }

    public void shareSelected() {
        if(!TRACKER.hasSelection()) {
            return;
        }

        final ArrayList<Uri> uris = new ArrayList<>();

        for(Kapture kapture : getSelected()) {
            uris.add(Uri.parse(kapture.getLocation()));
        }

        KFile.shareVideoFiles(CONTEXT, uris);
    }

    public void wifiShareSelected() {
        if(!TRACKER.hasSelection()) {
            return;
        }

        new WifiShare(CONTEXT, getSelected()).start();
    }

    public void renameSelected() {
        final Kapture kapture = getSelected().get(0);

        new InputPopup.Text(
            CONTEXT,
            R.string.popup_btn_rename,
            KFile.removeFileExtension(kapture.getName()),
            R.string.popup_btn_rename,
            new InputPopup.OnInputPopupListener() {
                @Override
                public void onStringInputSet(String input) {
                    unselectAll();

                    final File f = KFile.renameFile(input, kapture.getLocation());

                    kapture.setLocation(f.getAbsolutePath());

                    for(Kapture.Extra extra : kapture.getExtras()) {
                        final File fe = KFile.renameFile(input + KFile.FILE_SEPARATOR + extra.getFileNameComplement(CONTEXT), extra.getLocation());

                        extra.setLocation(fe.getAbsolutePath());
                    }

                    for(Kapture.Screenshot screenshot : kapture.getScreenshots()) {
                        final File fs = KFile.renameFile(input + KFile.FILE_SEPARATOR + KFile.getDefaultScreenshotName(CONTEXT), screenshot.getLocation());

                        screenshot.setLocation(fs.getAbsolutePath());
                    }

                    DATABASE.updateKapture(kapture);

                    clearSearch();

                    Toast.makeText(CONTEXT, getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();
                }
            },
            R.string.popup_btn_cancel,
            null,
            true,
            false,
            false
        ).show();
    }

    public void removeSelected() {
        new DialogPopup(
            CONTEXT,
            DialogPopup.NO_TEXT,
            getString(R.string.popup_remove),
            R.string.popup_btn_remove,
            () -> {
                final ArrayList<Kapture> toRemove = getSelected();

                for(Kapture kapture : toRemove) {
                    DATABASE.deleteKapture(kapture);

                    if(kapture.getThumbnailCachedFile().exists()) {
                        kapture.getThumbnailCachedFile().delete();
                    }
                }

                unselectAll();

                KAPTURES.removeAll(toRemove);

                clearSearch();

                setEmptyAdapterIfEmpty();

                updateSubtitle();

                Toast.makeText(CONTEXT, getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();
            },
            R.string.popup_btn_cancel,
            null,
            false,
            false,
            true
        ).show();
    }

    public void deleteSelectedExtras() {
        new DialogPopup(
            CONTEXT,
            DialogPopup.NO_TEXT,
            getString(R.string.popup_delete_text_3),
            R.string.popup_btn_delete,
            () -> {
                for(long index : TRACKER.getSelection()) {
                    final Kapture kapture = KAPTURES.get((int) index);

                    DATABASE.deleteExtras(kapture);

                    for(Kapture.Extra extra : kapture.getExtras()) {
                        final File f = new File(extra.getLocation());

                        if(f.exists()) {
                            f.delete();
                        }
                    }

                    kapture.setExtras(null);
                }

                unselectAll();

                clearSearch();

                Toast.makeText(CONTEXT, getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();
            },
            R.string.popup_btn_cancel,
            null,
            false,
            false,
            true
        ).show();
    }

    public void deleteSelectedScreenshots() {
        new DialogPopup(
            CONTEXT,
            DialogPopup.NO_TEXT,
            getString(R.string.popup_delete_text_4),
            R.string.popup_btn_delete,
            () -> {
                for(long index : TRACKER.getSelection()) {
                    final Kapture kapture = KAPTURES.get((int) index);

                    DATABASE.deleteScreenshots(kapture);

                    for(Kapture.Screenshot screenshot : kapture.getScreenshots()) {
                        final File f = new File(screenshot.getLocation());

                        if(f.exists()) {
                            f.delete();
                        }
                    }

                    kapture.setScreenshots(null);
                }

                unselectAll();

                clearSearch();

                Toast.makeText(CONTEXT, getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();
            },
            R.string.popup_btn_cancel,
            null,
            false,
            false,
            true
        ).show();
    }

    public void showSelectedExtras() {
        final Kapture kapture = getSelected().get(0);

        if(kapture.hasExtras()) {
            new ExtraPopup(CONTEXT, kapture.getExtras(), null).show();
        } else {
            Toast.makeText(CONTEXT, getString(R.string.toast_error_no_extras_found), Toast.LENGTH_SHORT).show();
        }
    }

    public void showSelectedScreenshots() {
        final Kapture kapture = getSelected().get(0);

        if(kapture.hasScreenshots()) {
            new ScreenshotPopup(CONTEXT, kapture.getScreenshots(), null).show();
        } else {
            Toast.makeText(CONTEXT, getString(R.string.toast_error_no_screenshots_found), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean selectedHasExtra() {
        return getSelected().get(0).hasExtras();
    }

    public boolean selectedHasScreenshot() {
        return getSelected().get(0).hasScreenshots();
    }

    public void openSelectedWith() {
        KFile.openFile(CONTEXT, getSelected().get(0).getLocation(), true);
    }

    public void updateUI(Kapture kapture) {
        new Handler(Looper.getMainLooper()).post(() -> {
            requestFloatingButtonUpdate();

            if(kapture != null) {
                KAPTURES.add(kapture);

                if(KAPTURES.size() == 1) {
                    setEmptyAdapterIfEmpty();
                }

                sortAdapter(true);
            }

            updateSubtitle();
        });
    }

    public void requestFloatingButtonUpdate() {
        if(CapturingService.isProcessing() || CapturingService.isInCountdown()) {
            BTN_FLOATING_START_STOP.setVisibility(View.GONE);
            BTN_FLOATING_PAUSE_RESUME.setVisibility(View.GONE);

            return;
        } else {
            BTN_FLOATING_START_STOP.setVisibility(View.VISIBLE);

            BTN_FLOATING_PAUSE_RESUME.setVisibility(CapturingService.isRecording() ? View.VISIBLE : View.GONE);
        }

        if(IS_TABLET_UI) {
            BTN_FLOATING_START_STOP.setText(CapturingService.isRecording() ? R.string.floating_btn_stop : R.string.floating_btn_start);
            BTN_FLOATING_PAUSE_RESUME.setText(CapturingService.isPaused() ? R.string.floating_btn_resume : R.string.floating_btn_pause);
        }

        if(CapturingService.isRecording()) {
            BTN_FLOATING_START_STOP.setTooltipText(getString(R.string.tooltip_stop));

            BTN_FLOATING_START_STOP.setCompoundDrawablesWithIntrinsicBounds(Utils.getDrawable(CONTEXT, R.drawable.icon_capture_stop), null, null, null);
        } else {
            BTN_FLOATING_START_STOP.setTooltipText(getString(R.string.tooltip_start));

            BTN_FLOATING_START_STOP.setCompoundDrawablesWithIntrinsicBounds(Utils.getDrawable(CONTEXT, R.drawable.icon_capture_start), null, null, null);
        }

        if(CapturingService.isPaused()) {
            BTN_FLOATING_PAUSE_RESUME.setTooltipText(getString(R.string.tooltip_resume));
            BTN_FLOATING_PAUSE_RESUME.setCompoundDrawablesWithIntrinsicBounds(Utils.getDrawable(CONTEXT, R.drawable.icon_capture_resume), null, null, null);
        } else {
            BTN_FLOATING_PAUSE_RESUME.setTooltipText(getString(R.string.tooltip_pause));
            BTN_FLOATING_PAUSE_RESUME.setCompoundDrawablesWithIntrinsicBounds(Utils.getDrawable(CONTEXT, R.drawable.icon_capture_pause), null, null, null);
        }
    }

    public void setCaptureButtonVisibility(boolean hide) {
        BTN_FLOATING_START_STOP.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    private void updateSubtitle() {
        SUBTITLE.setText(KAPTURES.size() + " " + CONTEXT.getString(KAPTURES.size() == 1 ? R.string.kapture : R.string.kapture_plural));
    }

    public void openSettings_tabletUi() {
        if(IS_TABLET_UI) {
            getChildFragmentManager().beginTransaction().show(SETTINGS_FRAGMENT__TABLE_UI).commitNow();
            VIEW.findViewById(R.id.frameLayoutContainer).setVisibility(View.VISIBLE);
            SETTINGS_FRAGMENT__TABLE_UI.onResume();
        }
    }
}
