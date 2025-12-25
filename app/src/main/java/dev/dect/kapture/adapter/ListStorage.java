package dev.dect.kapture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.KFile;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class ListStorage {
    public static class StorageItem {
        private final int colorId,
                          labelId;

        private long value;

        public StorageItem(int colorId, int labelId, long value) {
            this.labelId = labelId;
            this.colorId = colorId;
            this.value = value;
        }

        public int getLabelId() {
            return labelId;
        }

        public int getColorId() {
            return colorId;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    public static class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
        private final String TAG = ListStorage.class.getSimpleName() + "." + Adapter.class.getSimpleName();

        final ArrayList<StorageItem> ITEMS = new ArrayList<>();

        long ITEMS_VALUES_SUM;

        final boolean IS_LAST_FROM_GROUP;

        public Adapter(Context ctx, boolean isLastFromGroup) {
            this.IS_LAST_FROM_GROUP = isLastFromGroup;

            this.ITEMS.add(new ListStorage.StorageItem(R.color.storage_other, R.string.storage_other, 0));
            this.ITEMS.add(new ListStorage.StorageItem(R.color.storage_kaptures, R.string.storage_kaptures, 0));
            this.ITEMS.add(new ListStorage.StorageItem(R.color.storage_cache, R.string.storage_cache, 0));
            this.ITEMS.add(new ListStorage.StorageItem(R.color.storage_free, R.string.storage_free, 0));

            updateValues(ctx);

            updateItemsValueSum();
        }

        public void updateValues(Context ctx) {
            final long storageTotal = ctx.getFilesDir().getTotalSpace(),
                       storageFree = ctx.getFilesDir().getFreeSpace(),
                       storageKaptures = KFile.getAppTotalFilesSize(ctx, false),
                       storageCache = KFile.getCacheSize(ctx);

            ITEMS.get(0).setValue(storageTotal - storageFree - storageKaptures - storageCache);
            ITEMS.get(1).setValue(storageKaptures);
            ITEMS.get(2).setValue(storageCache);
            ITEMS.get(3).setValue(storageFree);
        }

        private void updateItemsValueSum() {
            ITEMS_VALUES_SUM = 0;

            for(StorageItem s : ITEMS) {
                ITEMS_VALUES_SUM += s.getValue();
            }
        }

        private long getItemsValueSum() {
            return ITEMS_VALUES_SUM;
        }

        private boolean isLastItemFromGroup() {
            return IS_LAST_FROM_GROUP;
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            private final ConstraintLayout EL_CONTAINER;

            private final ImageView EL_BAR;

            private final RecyclerView EL_RECYCLER_VIEW;

            public MyViewHolder(View view) {
                super(view);

                this.EL_CONTAINER = view.findViewById(R.id.container);

                this.EL_BAR = view.findViewById(R.id.bar);

                this.EL_RECYCLER_VIEW = view.findViewById(R.id.barData);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_storage, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Context ctx = holder.EL_CONTAINER.getContext();

            if(!isLastItemFromGroup()) {
                holder.EL_CONTAINER.setBackgroundResource(R.drawable.list_item_divisor_horizontal_bottom);
            }

            holder.EL_RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(ctx));
            holder.EL_RECYCLER_VIEW.setNestedScrollingEnabled(false);
            holder.EL_RECYCLER_VIEW.setAdapter(new ListStorageItemAdapter(ITEMS));

            holder.EL_BAR.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    try {
                        final int h = holder.EL_BAR.getHeight(),
                                  w = holder.EL_BAR.getWidth();

                        final Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

                        final Canvas canvas = new Canvas(bitmap);

                        final Paint paint = new Paint();

                        paint.setStyle(Paint.Style.FILL);

                        paint.setColor(Color.BLACK);

                        canvas.drawRoundRect(0, 0, w, h, 150, 150, paint);

                        paint.setBlendMode(BlendMode.SRC_IN);

                        int startAt = 0;

                        for(StorageItem storageItem : ITEMS) {
                            int length = (int) ((w * storageItem.getValue()) / getItemsValueSum());

                            if(length <= 0) {
                                continue;
                            }

                            final int endAt = startAt + length;

                            paint.setColor(ctx.getColor(storageItem.getColorId()));

                            canvas.drawRect(startAt, 0, endAt, h, paint);

                            startAt = endAt;
                        }

                        holder.EL_BAR.setImageBitmap(bitmap);

                        holder.EL_BAR.getViewTreeObserver().removeOnPreDrawListener(this);
                    } catch (Exception e) {
                        Log.e(TAG, "onPreDraw: " + e.getMessage());
                    }

                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }
}
