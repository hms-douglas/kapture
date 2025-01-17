package dev.dect.kapture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.service.CapturingService;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class ListButtonSubTextSwitch {
    public interface OnListButtonSubText {
        void onButtonClicked(ListButtonSubTextSwitch listButtonSubText);
    }

    private final int ID_TITLE;

    private String VALUE;

    private final String SP_KEY;

    private boolean IS_ENABLED;

    private final boolean IS_LAST_FROM_GROUP;

    private final OnListButtonSubText LISTENER;

    public ListButtonSubTextSwitch(int title, String value, OnListButtonSubText onListButtonSubText, String key, boolean b, boolean lastFromGroup) {
        this.ID_TITLE = title;
        this.VALUE = value;
        this.IS_LAST_FROM_GROUP = lastFromGroup;
        this.LISTENER = onListButtonSubText;
        this.SP_KEY = key;
        this.IS_ENABLED = b;
    }

    public ListButtonSubTextSwitch(int title, int value, OnListButtonSubText onClick, String key, boolean b, boolean lastFromGroup) {
        this.ID_TITLE = title;
        this.VALUE = String.valueOf(value);
        this.IS_LAST_FROM_GROUP = lastFromGroup;
        this.LISTENER = onClick;
        this.SP_KEY = key;
        this.IS_ENABLED = b;
    }

    public ListButtonSubTextSwitch(int title, float value, OnListButtonSubText onClick, String key, boolean b, boolean lastFromGroup) {
        this.ID_TITLE = title;
        this.VALUE = String.valueOf(value);
        this.IS_LAST_FROM_GROUP = lastFromGroup;
        this.LISTENER = onClick;
        this.SP_KEY = key;
        this.IS_ENABLED = b;
    }

    public int getIdTitle() {
        return ID_TITLE;
    }

    public String getValue() {
        return VALUE;
    }

    public void setValue(String v) {
        this.VALUE = v;
    }

    public void setValue(int v) {
        this.VALUE = String.valueOf(v);
    }

    public void setValue(float v) {
        this.VALUE = String.valueOf(v);
    }

    public boolean isLastItemFromGroup(){
        return IS_LAST_FROM_GROUP;
    }

    public boolean isEnabled() {
        return IS_ENABLED;
    }

    public void setEnabled(boolean b) {
        this.IS_ENABLED = b;
    }

    public String getSpKey() {
        return SP_KEY;
    }

    private OnListButtonSubText getListener() {
        return LISTENER;
    }

    public static class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
        private final ArrayList<ListButtonSubTextSwitch> LIST_BUTTONS;

        private final boolean IS_APP_SETTINGS;

        public Adapter(ArrayList<ListButtonSubTextSwitch> listButtonSubTexts, boolean isAppSettings) {
            this.LIST_BUTTONS = listButtonSubTexts;
            this.IS_APP_SETTINGS = isAppSettings;
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            private final ConstraintLayout EL_CONTAINER,
                                           EL_BTN;

            private final TextView EL_TITLE,
                                   EL_SUB_TITLE;

            private final Switch EL_SWITCH;

            public MyViewHolder(View view) {
                super(view);

                this.EL_CONTAINER = view.findViewById(R.id.listContainer);

                this.EL_BTN = view.findViewById(R.id.btn);

                this.EL_TITLE = view.findViewById(R.id.itemTitle);
                this.EL_SUB_TITLE = view.findViewById(R.id.itemSubTitle);

                this.EL_SWITCH = view.findViewById(R.id.switchBtn);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_button_sub_text_switch, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final ListButtonSubTextSwitch listButtonSubTextSwitch = LIST_BUTTONS.get(position);

            final Context ctx = holder.EL_CONTAINER.getContext();

            holder.EL_TITLE.setText(ctx.getString(listButtonSubTextSwitch.getIdTitle()));

            holder.EL_SUB_TITLE.setText(listButtonSubTextSwitch.getValue());

            if(!listButtonSubTextSwitch.isLastItemFromGroup()) {
                holder.EL_CONTAINER.setBackgroundResource(R.drawable.list_item_divisor_horizontal_bottom);
            }

            holder.EL_BTN.setOnClickListener((l) -> {
                if(CapturingService.isRecording()) {
                    Toast.makeText(ctx, ctx.getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

                    return;
                }

                listButtonSubTextSwitch.getListener().onButtonClicked(listButtonSubTextSwitch);
            });

            holder.EL_SWITCH.setChecked(listButtonSubTextSwitch.isEnabled());

            holder.EL_SWITCH.setOnCheckedChangeListener((v, b) -> {
                if(CapturingService.isRecording()) {
                    v.setChecked(listButtonSubTextSwitch.isEnabled());

                    Toast.makeText(ctx, ctx.getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

                    return;
                }

                listButtonSubTextSwitch.setEnabled(b);

                (IS_APP_SETTINGS ? KSharedPreferences.getAppSp(ctx) : KSharedPreferences.getActiveProfileSp(ctx)).edit().putBoolean(listButtonSubTextSwitch.getSpKey(), b).apply();
            });
        }

        @Override
        public int getItemCount() {
            return LIST_BUTTONS.size();
        }
    }
}
