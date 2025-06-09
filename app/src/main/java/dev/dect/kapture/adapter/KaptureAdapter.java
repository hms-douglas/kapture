package dev.dect.kapture.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DefaultSettings;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.fragment.KapturesFragment;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.popup.ExtraPopup;
import dev.dect.kapture.popup.ScreenshotPopup;
import dev.dect.kapture.utils.KFile;

@SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
public class KaptureAdapter extends RecyclerView.Adapter<KaptureAdapter.MyViewHolder> implements Filterable {
    private final ArrayList<Kapture> INITIAL_LIST_KAPTURES,
                                     LIST_KAPTURES;

    private SelectionTracker<Long> TRACKER;

    private final Filter FILTER = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence input) {
            final ArrayList<Kapture> filteredList = new ArrayList<>();

            if(input == null || input.toString().trim().isEmpty()) {
                filteredList.addAll(INITIAL_LIST_KAPTURES);
            } else {
                final String search = input.toString().toLowerCase();

                for(Kapture kapture : INITIAL_LIST_KAPTURES) {
                    if(kapture.getName().toLowerCase().contains(search)) {
                        filteredList.add(kapture);
                    }
                }
            }

            final FilterResults results = new FilterResults();

            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence input, FilterResults results) {
            if(results.values instanceof ArrayList) {
                LIST_KAPTURES.clear();
                LIST_KAPTURES.addAll((ArrayList<Kapture>) results.values);

                notifyDataSetChanged();
            }
        }
    };

    @Override
    public Filter getFilter() {
        return FILTER;
    }

    public KaptureAdapter(ArrayList<Kapture> listKaptures) {
        this.INITIAL_LIST_KAPTURES = listKaptures;

        this.LIST_KAPTURES = new ArrayList<>(listKaptures);

        setHasStableIds(true);
    }

    public void setTracker(SelectionTracker<Long> tracker) {
        this.TRACKER = tracker;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout EL_CONTAINER;

        private final ImageView EL_SELECTOR,
                                EL_THUMBNAIL,
                                EL_FROM;

        private final TextView EL_NAME,
                               EL_DURATION;

        private final int STYLE;

        private TextView EL_RESOLUTION,
                         EL_DATE,
                         EL_FRAMES,
                         EL_SIZE;

        private final AppCompatButton EL_EXTRA,
                                      EL_SCREENSHOT;

        public MyViewHolder(View view) {
            super(view);

            this.STYLE = KSharedPreferences.getAppSp(view.getContext()).getInt(Constants.Sp.App.LAYOUT_MANAGER_STYLE, DefaultSettings.LAYOUT_MANAGER_STYLE);

            this.EL_CONTAINER = view.findViewById(R.id.kapture);
            this.EL_THUMBNAIL = view.findViewById(R.id.thumbnail);
            this.EL_NAME = view.findViewById(R.id.name);
            this.EL_DURATION = view.findViewById(R.id.duration);
            this.EL_SELECTOR = view.findViewById(R.id.select);
            this.EL_EXTRA = view.findViewById(R.id.btnExtra);
            this.EL_SCREENSHOT = view.findViewById(R.id.btnScreenshot);
            this.EL_FROM = view.findViewById(R.id.fromIcon);

            if(this.STYLE == KapturesFragment.STYLE_LIST) {
                this.EL_RESOLUTION = view.findViewById(R.id.resolution);
                this.EL_DATE = view.findViewById(R.id.date);
                this.EL_FRAMES = view.findViewById(R.id.frames);
                this.EL_SIZE = view.findViewById(R.id.size);
            }
        }

        public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            return new ItemDetailsLookup.ItemDetails<Long>() {
                @Override
                public int getPosition() {
                    return getBindingAdapterPosition();
                }

                @Override
                public Long getSelectionKey() {
                    return getItemId();
                }
            };
        }
    }

    public static class ItemLookup extends ItemDetailsLookup<Long> {
        private final RecyclerView RECYCLER_VIEW;

        public ItemLookup(RecyclerView recyclerView) {
            this.RECYCLER_VIEW = recyclerView;
        }

        @Nullable
        @Override
        public ItemDetails<Long> getItemDetails(@NonNull MotionEvent event) {
            final View view = RECYCLER_VIEW.findChildViewUnder(event.getX(), event.getY());

            if(view != null) {
                final RecyclerView.ViewHolder viewHolder = RECYCLER_VIEW.getChildViewHolder(view);

                if(viewHolder instanceof KaptureAdapter.MyViewHolder) {
                    return ((KaptureAdapter.MyViewHolder) viewHolder).getItemDetails();
                }
            }
            return null;
        }
    }

    @NonNull
    @Override
    public KaptureAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch(KSharedPreferences.getAppSp(parent.getContext()).getInt(Constants.Sp.App.LAYOUT_MANAGER_STYLE, DefaultSettings.LAYOUT_MANAGER_STYLE)) {
            case KapturesFragment.STYLE_GRID_BIG:
                return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_kapture_grid_big, parent, false));

            case KapturesFragment.STYLE_GRID_SMALL:
                return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_kapture_grid_small, parent, false));

            default:
                return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_kapture_list, parent, false));
        }
     }

    @Override
    public void onBindViewHolder(@NonNull KaptureAdapter.MyViewHolder holder, int position) {
        final Kapture kapture = LIST_KAPTURES.get(position);

        final File file = new File(kapture.getLocation());

        final Context ctx = holder.EL_CONTAINER.getContext();

        holder.EL_CONTAINER.setOnClickListener((v) -> KFile.openFile(ctx, file));

        holder.EL_CONTAINER.setOnLongClickListener((v) -> {
            if(TRACKER.isSelected((long) position)) {
                return false;
            }

            TRACKER.select((long) position);

            return true;
        });

        holder.EL_NAME.setText(kapture.getName());

        holder.EL_FROM.setVisibility(kapture.isFromWatch() ? View.VISIBLE : View.GONE);

        if(kapture.hasExtras()) {
            holder.EL_EXTRA.setVisibility(View.VISIBLE);

            holder.EL_EXTRA.setOnClickListener((v) -> {
                if(kapture.hasExtras()) {
                    new ExtraPopup(ctx, kapture.getExtras(), null).show();
                } else {
                    holder.EL_EXTRA.setVisibility(View.GONE);

                    Toast.makeText(ctx, ctx.getString(R.string.toast_error_no_extras_found), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.EL_EXTRA.setVisibility(View.GONE);
        }

        if(kapture.hasScreenshots()) {
            holder.EL_SCREENSHOT.setVisibility(View.VISIBLE);

            holder.EL_SCREENSHOT.setOnClickListener((v) -> {
                if(kapture.hasScreenshots()) {
                    new ScreenshotPopup(ctx, kapture.getScreenshots(), null).show();
                } else {
                    holder.EL_SCREENSHOT.setVisibility(View.GONE);

                    Toast.makeText(ctx, ctx.getString(R.string.toast_error_no_screenshots_found), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.EL_SCREENSHOT.setVisibility(View.GONE);
        }

        if(holder.STYLE != KapturesFragment.STYLE_LIST) {
            kapture.retrieveAllMediaData(() -> {
                if(kapture.getThumbnail() != null) {
                    holder.EL_THUMBNAIL.setImageBitmap(kapture.getThumbnail());
                } else {
                    holder.EL_THUMBNAIL.setImageResource(R.drawable.icon_kapture_image_error_helper);
                }

                try {
                    holder.EL_DURATION.setText(KFile.formatFileDuration(kapture.getDuration()));
                    holder.EL_DURATION.setVisibility(View.VISIBLE);
                } catch (Exception ignore) {}
            });

            holder.EL_SELECTOR.setImageResource(TRACKER.isSelected((long) position) ? R.drawable.checkbox_on : R.drawable.checkbox_off);
        } else {
            holder.EL_SIZE.setText(KFile.formatFileSize(kapture.getSize()));
            holder.EL_SIZE.setVisibility(View.VISIBLE);

            holder.EL_DATE.setText(KFile.formatFileDate(kapture.getCreationTime()));
            holder.EL_DATE.setVisibility(View.VISIBLE);

            kapture.retrieveAllMediaData(() -> {
                try {
                    if(kapture.getThumbnail() != null) {
                        holder.EL_THUMBNAIL.setImageBitmap(kapture.getThumbnail());
                    } else {
                        holder.EL_THUMBNAIL.setImageResource(R.drawable.icon_kapture_image_error_helper);
                    }

                    final int[] size = kapture.getVideoSize();

                    holder.EL_RESOLUTION.setText(size[0] + "x" + size[1]);
                    holder.EL_RESOLUTION.setVisibility(View.VISIBLE);

                    holder.EL_DURATION.setText(KFile.formatFileDuration(kapture.getDuration()));
                    holder.EL_DURATION.setVisibility(View.VISIBLE);
                } catch (Exception ignore) {}
            });

            try {
                final MediaExtractor extractor = new MediaExtractor();

                extractor.setDataSource(kapture.getLocation());

                for(int i = 0; i < extractor.getTrackCount(); ++i) {
                    final MediaFormat format = extractor.getTrackFormat(i);

                    if(format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                        if(format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                            holder.EL_FRAMES.setText(format.getInteger(MediaFormat.KEY_FRAME_RATE) + " fps");
                            holder.EL_FRAMES.setVisibility(View.VISIBLE);
                        }
                    }
                }
            } catch (Exception ignore) {}

            if(TRACKER.isSelected((long) position)) {
                holder.EL_SELECTOR.setImageResource(R.drawable.checkbox_on);
                holder.EL_CONTAINER.setBackgroundColor(ctx.getColor(R.color.select_background));
            } else {
                holder.EL_SELECTOR.setImageResource(R.drawable.checkbox_off);
                holder.EL_CONTAINER.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        TRACKER.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
            if(!TRACKER.getSelection().isEmpty()) {
                if(holder.EL_SELECTOR.getTag() == null) {
                    holder.EL_SELECTOR.setTag(true);

                    holder.EL_SELECTOR.setVisibility(View.VISIBLE);

                    final ValueAnimator in = ValueAnimator.ofInt(0, (int) ctx.getResources().getDimension(R.dimen.kapture_list_select_icon));

                    in.addUpdateListener(valueAnimator -> {
                        final int val = (Integer) valueAnimator.getAnimatedValue();

                        final ViewGroup.LayoutParams layoutParams = holder.EL_SELECTOR.getLayoutParams();

                        layoutParams.height = val;
                        layoutParams.width = val;

                        holder.EL_SELECTOR.setLayoutParams(layoutParams);
                    });

                    in.setDuration(350);

                    in.start();
                }
            } else {
                holder.EL_SELECTOR.setTag(null);

                final ValueAnimator out = ValueAnimator.ofInt((int) ctx.getResources().getDimension(R.dimen.kapture_list_select_icon), 0);

                out.addUpdateListener(valueAnimator -> {
                    int val = (Integer) valueAnimator.getAnimatedValue();

                    ViewGroup.LayoutParams layoutParams = holder.EL_SELECTOR.getLayoutParams();

                    layoutParams.height = val;
                    layoutParams.width = val;

                    holder.EL_SELECTOR.setLayoutParams(layoutParams);
                });

                out.setDuration(350);

                out.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    holder.EL_SELECTOR.setVisibility(View.GONE);
                    }
                });

                out.start();
            }
            }
        });
    }

    @Override
    public int getItemCount() {
        return LIST_KAPTURES.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
