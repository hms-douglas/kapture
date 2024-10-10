package dev.dect.kapture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.popup.PickerFontPopup;
import dev.dect.kapture.popup.PickerPopup;
import dev.dect.kapture.service.CapturingService;

@SuppressLint({"UseSwitchCompatOrMaterialCode", "ApplySharedPref"})
public class ListPicker {
    public interface OnListPickerListener {
        default void onStringItemPicked(String value) {}

        default void onIntItemPicked(int value) {}

        default void onFloatItemPicked(float value) {}

        default void onFontItemPicked(String path) {}
    }

    private final int ID_TITLE;

    private final OnListPickerListener LISTENER;

    private final String SP_KEY;

    private final boolean IS_LAST_FROM_GROUP;

    private String[] DISPLAY_NAMES;

    public ListPicker(int title, String key, boolean lastFromGroup, String[] displayNames, OnListPickerListener l) {
        this.ID_TITLE = title;
        this.SP_KEY = key;
        this.IS_LAST_FROM_GROUP = lastFromGroup;
        this.DISPLAY_NAMES = displayNames;
        this.LISTENER = l;
    }

    public int getIdTitle() {
        return ID_TITLE;
    }

    public String getSpKey() {
        return SP_KEY;
    }

    public boolean isLastItemFromGroup(){
        return IS_LAST_FROM_GROUP;
    }

    public String[] getDisplayNames() {
        return DISPLAY_NAMES;
    }

    public String getActiveDisplayName() {
        return DISPLAY_NAMES[getActiveIndex()];
    }

    public int getActiveIndex() {
        return 0;
    }

    private OnListPickerListener getListener() {
        return LISTENER;
    }

    public static class Text extends ListPicker {
        private String ACTIVE_VALUE;

        private final String[] VALUES;

        public Text(int title, String activeValue, String[] values, @Nullable String[] displayNames, String key, boolean lastFromGroup, OnListPickerListener listener) {
            super(title, key, lastFromGroup, displayNames, listener);

            this.ACTIVE_VALUE = activeValue;
            this.VALUES = values;
        }

        public Text(int title, String activeValue, String[] values, @Nullable String[] displayNames, String key, boolean lastFromGroup) {
            super(title, key, lastFromGroup, displayNames, null);

            this.ACTIVE_VALUE = activeValue;
            this.VALUES = values;

            if(displayNames == null) {
                super.DISPLAY_NAMES = values;
            }
        }

        public int getActiveIndex() {
            for(int i = 0; i < VALUES.length; i++) {
                if(VALUES[i].equals(ACTIVE_VALUE)) {
                    return i;
                }
            }

            return -1;
        }

        public String getActiveValue() {
            return ACTIVE_VALUE;
        }

        private void setActiveByIndex(int i) {
            ACTIVE_VALUE = VALUES[i];
        }
    }

    public static class NumberInteger extends ListPicker {
        private int ACTIVE_VALUE;

        private final int[] VALUES;

        public NumberInteger(int title, int activeValue, int[] values, @Nullable String[] displayNames, String key, boolean lastFromGroup, OnListPickerListener listener) {
            super(title, key, lastFromGroup, displayNames, listener);

            this.ACTIVE_VALUE = activeValue;
            this.VALUES = values;

            if(displayNames == null) {
                super.DISPLAY_NAMES = Arrays.stream(values).mapToObj(String::valueOf).toArray(String[]::new);
            }
        }

        public NumberInteger(int title, int activeValue, int[] values, @Nullable String[] displayNames, String key, boolean lastFromGroup) {
            super(title, key, lastFromGroup, displayNames, null);

            this.ACTIVE_VALUE = activeValue;
            this.VALUES = values;

            if(displayNames == null) {
                super.DISPLAY_NAMES = Arrays.stream(values).mapToObj(String::valueOf).toArray(String[]::new);
            }
        }

        public int getActiveIndex() {
            for(int i = 0; i < VALUES.length; i++) {
                if(VALUES[i] == ACTIVE_VALUE) {
                    return i;
                }
            }

            return -1;
        }

        public int getActiveValue() {
            return ACTIVE_VALUE;
        }

        private void setActiveByIndex(int i) {
            ACTIVE_VALUE = VALUES[i];
        }
    }

    public static class NumberFloat extends ListPicker {
        private float ACTIVE_VALUE;

        private final float[] VALUES;

        public NumberFloat(int title, float activeValue, float[] values, @Nullable String[] displayNames, String key, boolean lastFromGroup) {
            super(title, key, lastFromGroup, displayNames, null);

            this.ACTIVE_VALUE = activeValue;
            this.VALUES = values;

            if(displayNames == null) {
                displayNames = new String[values.length];

                for(int i = 0; i < values.length; i++) {
                    displayNames[i] = String.valueOf(values[i]);
                }

                super.DISPLAY_NAMES = displayNames;
            }
        }


        public int getActiveIndex() {
            for(int i = 0; i < VALUES.length; i++) {
                if(VALUES[i] == ACTIVE_VALUE) {
                    return i;
                }
            }

            return -1;
        }

        public float getActiveValue() {
            return ACTIVE_VALUE;
        }

        private void setActiveByIndex(int i) {
            ACTIVE_VALUE = VALUES[i];
        }

    }

    public static class Font extends ListPicker {
        private String ACTIVE_VALUE;

        private final String[] VALUES;

        public Font(int title, String activeValue, String[] values, @Nullable String[] displayNames, String key, boolean lastFromGroup, OnListPickerListener listener) {
            super(title, key, lastFromGroup, displayNames, listener);

            this.ACTIVE_VALUE = activeValue;
            this.VALUES = values;
        }

        public Font(int title, String activeValue, String[] values, @Nullable String[] displayNames, String key, boolean lastFromGroup) {
            super(title, key, lastFromGroup, displayNames, null);

            this.ACTIVE_VALUE = activeValue;
            this.VALUES = values;

            if(displayNames == null) {
                super.DISPLAY_NAMES = values;
            }
        }

        public int getActiveIndex() {
            for(int i = 0; i < VALUES.length; i++) {
                if(VALUES[i].equals(ACTIVE_VALUE)) {
                    return i;
                }
            }

            return -1;
        }

        public String getActiveValue() {
            return ACTIVE_VALUE;
        }

        public String[] getValues() {
            return VALUES;
        }

        private void setActiveByIndex(int i) {
            ACTIVE_VALUE = VALUES[i];
        }
    }


    public static class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
        private final ArrayList<ListPicker> LIST_PICKERS_INT;

        public Adapter(ArrayList<ListPicker> listPickers) {
            this.LIST_PICKERS_INT = listPickers;
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            private final ConstraintLayout EL_CONTAINER;

            private final TextView EL_TITLE,
                                   EL_SUB_TITLE;

            public MyViewHolder(View view) {
                super(view);

                this.EL_CONTAINER = view.findViewById(R.id.listContainer);

                this.EL_TITLE = view.findViewById(R.id.itemTitle);
                this.EL_SUB_TITLE = view.findViewById(R.id.itemSubTitle);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_picker, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final ListPicker listPicker = LIST_PICKERS_INT.get(position);

            final Context ctx = holder.EL_CONTAINER.getContext();

            holder.EL_TITLE.setText(ctx.getString(listPicker.getIdTitle()));

            holder.EL_SUB_TITLE.setText(listPicker.getActiveDisplayName());

            if(!listPicker.isLastItemFromGroup()) {
                holder.EL_CONTAINER.setBackgroundResource(R.drawable.list_item_divisor_horizontal_bottom);
            }

            if(listPicker instanceof ListPicker.Font) {
                holder.EL_SUB_TITLE.setTypeface(KSettings.getTypeFaceForFontPath(ctx, ((Font) listPicker).getActiveValue()));

                holder.EL_CONTAINER.setOnClickListener((l) -> {
                    if (CapturingService.isRecording()) {
                        Toast.makeText(ctx, ctx.getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    new PickerFontPopup(ctx, listPicker.getIdTitle(), listPicker.getDisplayNames(),  ((ListPicker.Font) listPicker).getValues(), listPicker.getActiveIndex(), (indexPicked) -> {
                        final SharedPreferences.Editor editor = ctx.getSharedPreferences(Constants.SP, Context.MODE_PRIVATE).edit();

                        final ListPicker.Font listFont = (ListPicker.Font) listPicker;

                        listFont.setActiveByIndex(indexPicked);

                        editor.putString(listPicker.getSpKey(), listFont.getActiveValue()).commit();

                        if (listPicker.getListener() != null) {
                            listPicker.getListener().onFontItemPicked(listFont.getActiveValue());
                        }

                        holder.EL_SUB_TITLE.setText(listPicker.getActiveDisplayName());

                        holder.EL_SUB_TITLE.setTypeface(KSettings.getTypeFaceForFontPath(ctx, listFont.getActiveValue()));
                    }).show();
                });
            } else {
                holder.EL_CONTAINER.setOnClickListener((l) -> {
                    if (CapturingService.isRecording()) {
                        Toast.makeText(ctx, ctx.getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    new PickerPopup(ctx, listPicker.getIdTitle(), listPicker.getDisplayNames(), listPicker.getActiveIndex(), (indexPicked) -> {
                        final SharedPreferences.Editor editor = ctx.getSharedPreferences(Constants.SP, Context.MODE_PRIVATE).edit();

                        if (listPicker instanceof ListPicker.Text) {
                            final ListPicker.Text listText = (ListPicker.Text) listPicker;

                            listText.setActiveByIndex(indexPicked);

                            editor.putString(listPicker.getSpKey(), listText.getActiveValue()).commit();

                            if (listPicker.getListener() != null) {
                                listPicker.getListener().onStringItemPicked(listText.getActiveValue());
                            }
                        } else if (listPicker instanceof ListPicker.NumberFloat) {
                            final ListPicker.NumberFloat listFloat = (ListPicker.NumberFloat) listPicker;

                            listFloat.setActiveByIndex(indexPicked);

                            editor.putFloat(listPicker.getSpKey(), listFloat.getActiveValue()).commit();

                            if (listPicker.getListener() != null) {
                                listPicker.getListener().onFloatItemPicked(listFloat.getActiveValue());
                            }
                        } else if (listPicker instanceof ListPicker.NumberInteger) {
                            final ListPicker.NumberInteger listInteger = (ListPicker.NumberInteger) listPicker;

                            listInteger.setActiveByIndex(indexPicked);

                            editor.putInt(listPicker.getSpKey(), listInteger.getActiveValue()).commit();

                            if (listPicker.getListener() != null) {
                                listPicker.getListener().onIntItemPicked(listInteger.getActiveValue());
                            }
                        }

                        holder.EL_SUB_TITLE.setText(listPicker.getActiveDisplayName());
                    }).show();
                });
            }
        }

        @Override
        public int getItemCount() {
            return LIST_PICKERS_INT.size();
        }
    }
}
