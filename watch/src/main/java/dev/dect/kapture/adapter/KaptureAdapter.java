package dev.dect.kapture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.utils.KFile;

@SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
public class KaptureAdapter extends RecyclerView.Adapter<KaptureAdapter.MyViewHolder> {
    private final ArrayList<Kapture> INITIAL_LIST_KAPTURES;

    private SelectionTracker<Long> TRACKER;

    public KaptureAdapter(ArrayList<Kapture> listKaptures) {
        this.INITIAL_LIST_KAPTURES = listKaptures;

        setHasStableIds(true);
    }

    public void setTracker(SelectionTracker<Long> tracker) {
        this.TRACKER = tracker;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout EL_CONTAINER,
                                       EL_DIVISOR;

        private final ImageView EL_SELECTOR,
                                EL_THUMBNAIL;

        private final TextView EL_NAME,
                               EL_DURATION,
                               EL_RESOLUTION,
                               EL_DATE,
                               EL_SIZE;

        public MyViewHolder(View view) {
            super(view);


            this.EL_CONTAINER = view.findViewById(R.id.kapture);
            this.EL_DIVISOR = view.findViewById(R.id.divisor);
            this.EL_THUMBNAIL = view.findViewById(R.id.thumbnail);
            this.EL_NAME = view.findViewById(R.id.name);
            this.EL_DURATION = view.findViewById(R.id.duration);
            this.EL_SELECTOR = view.findViewById(R.id.select);
            this.EL_RESOLUTION = view.findViewById(R.id.resolution);
            this.EL_DATE = view.findViewById(R.id.date);
            this.EL_SIZE = view.findViewById(R.id.size);
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

                if(viewHolder instanceof MyViewHolder) {
                    return ((MyViewHolder) viewHolder).getItemDetails();
                }
            }
            return null;
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_kapture_list, parent, false));
     }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Kapture kapture = INITIAL_LIST_KAPTURES.get(position);

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

        if(position == (getItemCount() - 1)) {
            holder.EL_DIVISOR.setBackground(null);
        } else {
            holder.EL_DIVISOR.setBackgroundResource(R.drawable.kapture_item_divisor_horizontal_bottom);
        }

        holder.EL_NAME.setText(kapture.getName());

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

                holder.EL_RESOLUTION.setText(size[0] + "px");
                holder.EL_RESOLUTION.setVisibility(View.VISIBLE);

                holder.EL_DURATION.setText(KFile.formatFileDuration(kapture.getDuration()));
                holder.EL_DURATION.setVisibility(View.VISIBLE);
            } catch (Exception ignore) {}
        });

        if(TRACKER.isSelected((long) position)) {
            holder.EL_SELECTOR.setImageResource(R.drawable.checkbox_on);
            holder.EL_CONTAINER.setBackgroundColor(ctx.getColor(R.color.select_background));
        } else {
            holder.EL_SELECTOR.setImageResource(R.drawable.checkbox_off);
            holder.EL_CONTAINER.setBackgroundColor(Color.TRANSPARENT);
        }

        TRACKER.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                if(!TRACKER.getSelection().isEmpty()) {
                    holder.EL_SELECTOR.setVisibility(View.VISIBLE);
                } else {
                    holder.EL_SELECTOR.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return INITIAL_LIST_KAPTURES.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
