package dev.dect.kapture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.popup.ColorPickerPopup;
import dev.dect.kapture.utils.Utils;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class ListButtonColor {
    public interface OnListButtonSubText {
        void onColorPicked(String color);
    }

    private final int ID_TITLE;

    private final boolean IS_LAST_FROM_GROUP;

    private final OnListButtonSubText LISTENER;

    private String COLOR;

    public ListButtonColor(int title, String color, OnListButtonSubText onListButtonSubText, boolean lastFromGroup) {
        this.ID_TITLE = title;
        this.COLOR = color;
        this.IS_LAST_FROM_GROUP = lastFromGroup;
        this.LISTENER = onListButtonSubText;
    }

    public int getIdTitle() {
        return ID_TITLE;
    }

    public void setColor(String color) {
        this.COLOR = color;
    }

    public String getColor() {
        return COLOR;
    }

    public int getColorInt() {
        return Utils.Converter.hexColorToInt(COLOR);
    }

    public boolean isLastItemFromGroup(){
        return IS_LAST_FROM_GROUP;
    }

    private OnListButtonSubText getListener() {
        return LISTENER;
    }

    public static class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
        private final ArrayList<ListButtonColor> LIST_BUTTONS;

        public Adapter(ArrayList<ListButtonColor> listButtonSubTexts) {
            this.LIST_BUTTONS = listButtonSubTexts;
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            private final ConstraintLayout EL_CONTAINER;

            private final ImageView EL_COLOR;

            private final TextView EL_TITLE,
                                   EL_SUB_TITLE;

            public MyViewHolder(View view) {
                super(view);

                this.EL_CONTAINER = view.findViewById(R.id.listContainer);
                this.EL_COLOR = view.findViewById(R.id.color);

                this.EL_TITLE = view.findViewById(R.id.itemTitle);
                this.EL_SUB_TITLE = view.findViewById(R.id.itemSubTitle);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_button_color, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final ListButtonColor listButtonColor = LIST_BUTTONS.get(position);

            final Context ctx = holder.EL_CONTAINER.getContext();

            holder.EL_TITLE.setText(ctx.getString(listButtonColor.getIdTitle()));

            holder.EL_SUB_TITLE.setText(listButtonColor.getColor());

            holder.EL_COLOR.setImageBitmap(getColorExampleBitmap(ctx, listButtonColor.getColorInt()));

            if(!listButtonColor.isLastItemFromGroup()) {
                holder.EL_CONTAINER.setBackgroundResource(R.drawable.list_item_divisor_horizontal_bottom);
            }

            holder.EL_CONTAINER.setOnClickListener((l) -> {
                new ColorPickerPopup(ctx, listButtonColor.getColor(), true, (color) -> {
                    listButtonColor.setColor(color);

                    holder.EL_SUB_TITLE.setText(color);

                    holder.EL_COLOR.setImageBitmap(getColorExampleBitmap(ctx, listButtonColor.getColorInt()));

                    listButtonColor.getListener().onColorPicked(color);
                }).show();
            });
        }

        @Override
        public int getItemCount() {
            return LIST_BUTTONS.size();
        }

        private Bitmap getColorExampleBitmap(Context ctx, int color) {
            final int colorSize = ctx.getResources().getDimensionPixelSize(R.dimen.list_button_color_example);

            final Bitmap bitmap = Bitmap.createBitmap(colorSize, colorSize, Bitmap.Config.ARGB_8888);

            final Canvas canvas = new Canvas(bitmap);

            Utils.drawTransparencyBackground(canvas);

            final Paint paintColor = new Paint();

            paintColor.setStyle(Paint.Style.FILL);
            paintColor.setColor(color);

            canvas.drawRect(new RectF(0, 0, colorSize, colorSize), paintColor);

            final Bitmap cuttedBitmap = Bitmap.createBitmap(colorSize, colorSize, Bitmap.Config.ARGB_8888);

            final Canvas canvasCut = new Canvas(cuttedBitmap);

            final Rect rect = new Rect(0, 0, colorSize, colorSize);

            final Paint paintCut = new Paint();

            paintCut.setAntiAlias(true);

            canvasCut.drawARGB(0, 0, 0, 0);

            canvasCut.drawCircle(colorSize / 2f, colorSize / 2f, colorSize / 2f, paintCut);

            paintCut.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            canvasCut.drawBitmap(bitmap, rect, rect, paintCut);

            return cuttedBitmap;
        }
    }
}
