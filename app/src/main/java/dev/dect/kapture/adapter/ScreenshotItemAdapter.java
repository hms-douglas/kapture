package dev.dect.kapture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.MainActivity;
import dev.dect.kapture.data.DB;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.popup.DialogPopup;
import dev.dect.kapture.popup.ScreenshotPopup;
import dev.dect.kapture.utils.KFile;

/** @noinspection ResultOfMethodCallIgnored*/
@SuppressLint("SetTextI18n")
public class ScreenshotItemAdapter extends RecyclerView.Adapter<ScreenshotItemAdapter.MyViewHolder> {
    private final ArrayList<Kapture.Screenshot> SCREENSHOTS;

    private final ScreenshotPopup POPUP;

    public ScreenshotItemAdapter(ArrayList<Kapture.Screenshot> screenshots, @Nullable ScreenshotPopup popup) {
        this.SCREENSHOTS = screenshots;
        this.POPUP = popup;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatButton EL_BTN_SHARE,
                                      EL_BTN_DELETE;

        private final ImageView EL_PREVIEW;

        public MyViewHolder(View view) {
            super(view);

            this.EL_PREVIEW = view.findViewById(R.id.preview);
            this.EL_BTN_SHARE = view.findViewById(R.id.btnShare);
            this.EL_BTN_DELETE = view.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public ScreenshotItemAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_screenshot_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ScreenshotItemAdapter.MyViewHolder holder, int position) {
        final Context ctx = holder.EL_PREVIEW.getContext();

        final Kapture.Screenshot screenshot = SCREENSHOTS.get(position);

        new Handler(Looper.getMainLooper()).post(() -> holder.EL_PREVIEW.setImageBitmap(BitmapFactory.decodeFile(screenshot.getLocation())));

        holder.EL_PREVIEW.setOnClickListener((v) -> KFile.openFile(ctx, screenshot.getLocation()));

        holder.EL_BTN_SHARE.setOnClickListener((v) -> KFile.shareFile(ctx, screenshot.getLocation()));

        holder.EL_BTN_DELETE.setOnClickListener((v) -> {
            final File f = new File(screenshot.getLocation());

            new DialogPopup(
                ctx,
                DialogPopup.NO_TEXT,
                ctx.getString(R.string.popup_delete_text_1) + " " + f.getName() + "?",
                R.string.popup_btn_delete,
                () -> {
                    new DB(ctx).deleteScreenshot(screenshot);

                    f.delete();

                    try{
                        this.SCREENSHOTS.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, SCREENSHOTS.size());
                    } catch (Exception ignore){}

                    if(SCREENSHOTS.size() <= 0) {
                        Objects.requireNonNull(POPUP).dismissWithAnimation();

                        try {
                            if(MainActivity.getInstance() != null) {
                                MainActivity.getInstance().getKaptureFragment().triggerRefresh();
                            }
                        } catch (Exception ignore) {}
                    }
                },
                R.string.popup_btn_cancel,
                null,
                false,
                false,
                true
            ).show();
        });
    }

    @Override
    public int getItemCount() {
        return SCREENSHOTS.size();
    }
}