package dev.dect.kapture.tile;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.wear.protolayout.ActionBuilders;
import androidx.wear.protolayout.ColorBuilders;
import androidx.wear.protolayout.DeviceParametersBuilders;
import androidx.wear.protolayout.DimensionBuilders;
import androidx.wear.protolayout.LayoutElementBuilders;
import androidx.wear.protolayout.ModifiersBuilders;
import androidx.wear.protolayout.ResourceBuilders;
import androidx.wear.protolayout.TimelineBuilders;
import androidx.wear.protolayout.material.Button;
import androidx.wear.protolayout.material.ButtonColors;
import androidx.wear.protolayout.material.Chip;
import androidx.wear.protolayout.material.ChipColors;
import androidx.wear.tiles.RequestBuilders;
import androidx.wear.tiles.TileBuilders;
import androidx.wear.tiles.TileService;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Locale;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.MainActivity;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.service.CapturingService;


public class FullTile extends TileService {
    private final String RESOURCES_VERSION = "v0",
                         ID_ICON_START = "i0",
                         ID_ICON_STOP = "i1",
                         ID_ICON_PAUSE = "i2",
                         ID_ICON_RESUME = "i3",
                         ID_ICON_APP = "i4";

    @NonNull
    @Override
    protected ListenableFuture<ResourceBuilders.Resources> onTileResourcesRequest(@NonNull RequestBuilders.ResourcesRequest requestParams) {
        final ResourceBuilders.Resources.Builder resources = new ResourceBuilders.Resources.Builder();

        addResourceHelper(resources, ID_ICON_START, R.drawable.icon_capture_start_tile);
        addResourceHelper(resources, ID_ICON_STOP, R.drawable.icon_capture_stop);
        addResourceHelper(resources, ID_ICON_PAUSE, R.drawable.icon_capture_pause);
        addResourceHelper(resources, ID_ICON_RESUME, R.drawable.icon_capture_resume);
        addResourceHelper(resources, ID_ICON_APP, R.drawable.icon_app_tile);

        return Futures.immediateFuture(resources.setVersion(RESOURCES_VERSION).build());
    }

    @NonNull
    protected ListenableFuture<TileBuilders.Tile> onTileRequest(@NonNull RequestBuilders.TileRequest requestParams) {
        switch(Objects.requireNonNull(requestParams.getState()).getLastClickableId()) {
            case Constants.Tile.Action.START:
                CapturingService.requestStartRecording(this);
                break;

            case Constants.Tile.Action.STOP:
                CapturingService.requestStopRecording();
                break;

            case Constants.Tile.Action.PAUSE:
                CapturingService.requestPauseRecording();
                break;

            case Constants.Tile.Action.RESUME:
                CapturingService.requestResumeRecording();
                break;
        }

        return Futures.immediateFuture(
            new TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(new TimelineBuilders.Timeline.Builder()
                .addTimelineEntry(new TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(new LayoutElementBuilders.Layout.Builder()
                        .setRoot(
                            layout()
                        ).build()
                    ).build()
                ).build()
            ).build()
        );
    }

    public static void requestUpdate(Context ctx) {
        TileService.getUpdater(ctx).requestUpdate(FullTile.class);
    }

    private void addResourceHelper(ResourceBuilders.Resources.Builder resources, String id, int resId) {
        resources.addIdToImageMapping(
            id,
            new ResourceBuilders.ImageResource.Builder()
            .setAndroidResourceByResId(
                new ResourceBuilders.AndroidImageResourceByResId.Builder()
                .setResourceId(resId)
                .build()
            ).build()
        );
    }

    private LayoutElementBuilders.LayoutElement layout() {
        if(CapturingService.isProcessing()) {
            return processingLayout();
        }

        if(CapturingService.isInCountdown()) {
            return countdownLayout();
        }

        if(CapturingService.isRecording()) {
            return recordingLayout();
        }

        return startLayout();
    }

    private LayoutElementBuilders.LayoutElement processingLayout() {
        return new LayoutElementBuilders.Box.Builder()
        .setWidth(DimensionBuilders.expand())
        .setHeight(DimensionBuilders.expand())
        .addContent(
            addText(R.string.processing)
        ).addContent(
            addLogo()
        ).build();
    }

    private LayoutElementBuilders.LayoutElement countdownLayout() {
        return new LayoutElementBuilders.Box.Builder()
        .setWidth(DimensionBuilders.expand())
        .setHeight(DimensionBuilders.expand())
        .addContent(
            addText(R.string.under_countdown)
        ).addContent(
            addLogo()
        ).build();
    }

    private LayoutElementBuilders.LayoutElement recordingLayout() {
        return new LayoutElementBuilders.Box.Builder()
        .setWidth(DimensionBuilders.expand())
        .setHeight(DimensionBuilders.expand())
        .addContent(
            new LayoutElementBuilders.Row.Builder()
            .setHeight(DimensionBuilders.wrap())
            .setWidth(DimensionBuilders.wrap())
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .addContent(
                new Button.Builder(
                    this,
                    new ModifiersBuilders.Clickable.Builder()
                    .setId(Constants.Tile.Action.STOP)
                    .setOnClick(
                        new ActionBuilders.LoadAction.Builder().build()
                    ).build()
                ).setIconContent(ID_ICON_STOP)
                .setButtonColors(
                    new ButtonColors(
                    getColor(R.color.btn_main_background),
                    getColor(R.color.btn_main_font)
                    )
                ).setSize(DimensionBuilders.dp(55))
                .build()
            ).addContent(
                new LayoutElementBuilders.Spacer.Builder()
                .setHeight(DimensionBuilders.dp(12))
                .setWidth(DimensionBuilders.dp(12))
                .build()
            ).addContent(
                new Button.Builder(
                    this,
                    new ModifiersBuilders.Clickable.Builder()
                        .setId(CapturingService.isPaused() ? Constants.Tile.Action.RESUME : Constants.Tile.Action.PAUSE)
                        .setOnClick(
                            new ActionBuilders.LoadAction.Builder().build()
                        ).build()
                ).setIconContent(CapturingService.isPaused() ? ID_ICON_RESUME : ID_ICON_PAUSE)
                .setButtonColors(
                    new ButtonColors(
                        getColor(R.color.btn_secondary_background),
                        getColor(R.color.btn_secondary_font)
                    )
                ).setSize(DimensionBuilders.dp(55))
                .build()
            ).build()
        ).addContent(
            addLogo()
        ).build();
    }

    private LayoutElementBuilders.LayoutElement startLayout() {
        return new LayoutElementBuilders.Box.Builder()
        .setWidth(DimensionBuilders.expand())
        .setHeight(DimensionBuilders.expand())
        .addContent(
            new Chip.Builder(
                this,
                new ModifiersBuilders.Clickable.Builder()
                .setId(Constants.Tile.Action.START)
                .setOnClick(
                    new ActionBuilders.LoadAction.Builder().build()
                ).build(),
                new DeviceParametersBuilders.DeviceParameters.Builder().build()
            ).setChipColors(
                new ChipColors(
                    new ColorBuilders.ColorProp.Builder().setArgb(getColor(R.color.btn_main_background)).build(),
                    new ColorBuilders.ColorProp.Builder().setArgb(getColor(R.color.btn_main_font)).build(),
                    new ColorBuilders.ColorProp.Builder().setArgb(getColor(R.color.btn_main_font)).build(),
                    new ColorBuilders.ColorProp.Builder().setArgb(Color.TRANSPARENT).build()
                )
            ).setPrimaryLabelContent(getString(R.string.btn_start).toUpperCase(Locale.ROOT))
            .setIconContent(ID_ICON_START)
            .setWidth(DimensionBuilders.wrap())
            .build()
        ).addContent(
            addLogo()
        ).build();
    }

    private LayoutElementBuilders.Row addLogo() {
        return new LayoutElementBuilders.Row.Builder()
        .setWidth(DimensionBuilders.expand())
        .setHeight(DimensionBuilders.expand())
        .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_BOTTOM)
        .addContent(
            new LayoutElementBuilders.Column.Builder()
            .setHeight(DimensionBuilders.wrap())
            .setWidth(DimensionBuilders.expand())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .addContent(
                new LayoutElementBuilders.Image.Builder()
                .setWidth(DimensionBuilders.dp(23))
                .setHeight(DimensionBuilders.dp(23))
                .setResourceId(ID_ICON_APP)
                .setModifiers(
                    new ModifiersBuilders.Modifiers.Builder()
                    .setClickable(
                        new ModifiersBuilders.Clickable.Builder()
                        .setOnClick(
                            new ActionBuilders.LaunchAction.Builder()
                            .setAndroidActivity(
                                new ActionBuilders.AndroidActivity.Builder()
                                .setClassName(MainActivity.class.getName())
                                .setPackageName(getPackageName())
                                .build()
                            ).build()
                        ).build()
                    ).build()
                ).build()
            ).addContent(
                new LayoutElementBuilders.Spacer.Builder()
                .setWidth(DimensionBuilders.dp(10))
                .setHeight(DimensionBuilders.dp(10))
                .build()
            ).build()
        ).build();
    }

    private Chip addText(int stringResId) {
        return new Chip.Builder(
            this,
            new ModifiersBuilders.Clickable.Builder().build(),
            new DeviceParametersBuilders.DeviceParameters.Builder().build()
        ).setChipColors(
            new ChipColors(
                new ColorBuilders.ColorProp.Builder().setArgb(getColor(R.color.tile_message_background)).build(),
                new ColorBuilders.ColorProp.Builder().setArgb(getColor(R.color.tile_message_font)).build(),
                new ColorBuilders.ColorProp.Builder().setArgb(getColor(R.color.tile_message_font)).build(),
                new ColorBuilders.ColorProp.Builder().setArgb(Color.TRANSPARENT).build()
            )
        ).setPrimaryLabelContent(getString(stringResId))
        .setWidth(DimensionBuilders.wrap())
        .build();
    }
}



