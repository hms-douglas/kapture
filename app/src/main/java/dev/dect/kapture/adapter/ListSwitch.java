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

@SuppressLint({"UseSwitchCompatOrMaterialCode", "ApplySharedPref"})
public class ListSwitch {
    public interface OnListSwitchListener {
        default void onChange(boolean b) {}
    }

    public static final int NO_TEXT = -1;

    private final int ID_TITLE,
                      ID_SUB_TITLE;

    private final String SP_KEY;

    private boolean IS_ENABLED;

    private final boolean IS_LAST_FROM_GROUP;

    private final OnListSwitchListener LISTENER;

    public ListSwitch(int title, int subTitle, String key, boolean b, boolean lastFromGroup) {
        this.ID_TITLE = title;
        this.ID_SUB_TITLE = subTitle;
        this.SP_KEY = key;
        this.IS_ENABLED = b;
        this.IS_LAST_FROM_GROUP = lastFromGroup;
        this.LISTENER = null;
    }

    public ListSwitch(int title, int subTitle, String key, boolean b, OnListSwitchListener listener, boolean lastFromGroup) {
        this.ID_TITLE = title;
        this.ID_SUB_TITLE = subTitle;
        this.SP_KEY = key;
        this.IS_ENABLED = b;
        this.IS_LAST_FROM_GROUP = lastFromGroup;
        this.LISTENER = listener;
    }

    public int getIdTitle() {
        return ID_TITLE;
    }

    public int getIdSubTitle() {
        return ID_SUB_TITLE;
    }

    public boolean hasSubTitle() {
        return this.getIdSubTitle() != NO_TEXT;
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

    public boolean isLastItemFromGroup(){
        return IS_LAST_FROM_GROUP;
    }

    public OnListSwitchListener getListener() {
        return LISTENER;
    }

    public static class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
        private final ArrayList<ListSwitch> LIST_SWITCHES;

        private final boolean IS_APP_SETTINGS;

        public Adapter(ArrayList<ListSwitch> listSwitches, boolean isAppSettings) {
            this.LIST_SWITCHES = listSwitches;
            this.IS_APP_SETTINGS = isAppSettings;
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            private final ConstraintLayout EL_CONTAINER;

            private final Switch EL_SWITCH;

            private final TextView EL_TITLE,
                                   EL_SUB_TITLE;

            public MyViewHolder(View view) {
                super(view);

                this.EL_CONTAINER = view.findViewById(R.id.switchContainer);

                this.EL_SWITCH = view.findViewById(R.id.switchBtn);

                this.EL_TITLE = view.findViewById(R.id.switchTitle);
                this.EL_SUB_TITLE = view.findViewById(R.id.switchSubTitle);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_switch, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final ListSwitch listSwitch = LIST_SWITCHES.get(position);

            final Context ctx = holder.EL_SWITCH.getContext();

            holder.EL_TITLE.setText(ctx.getString(listSwitch.getIdTitle()));

            if(listSwitch.hasSubTitle()) {
                holder.EL_SUB_TITLE.setText(ctx.getString(listSwitch.getIdSubTitle()));
            } else {
                holder.EL_SUB_TITLE.setVisibility(View.GONE);
            }

            if(!listSwitch.isLastItemFromGroup()) {
                holder.EL_CONTAINER.setBackgroundResource(R.drawable.list_item_divisor_horizontal_bottom);
            }

            holder.EL_SWITCH.setChecked(listSwitch.isEnabled());

            holder.EL_CONTAINER.setOnClickListener((l) -> holder.EL_SWITCH.toggle());

            holder.EL_SWITCH.setOnCheckedChangeListener((v, b) -> {
                if(CapturingService.isRecording()) {
                    v.setChecked(listSwitch.isEnabled());

                    Toast.makeText(ctx, ctx.getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

                    return;
                }

                listSwitch.setEnabled(b);

                (IS_APP_SETTINGS ? KSharedPreferences.getAppSp(ctx) : KSharedPreferences.getActiveProfileSp(ctx)).edit().putBoolean(listSwitch.getSpKey(), b).commit();

                if(listSwitch.LISTENER != null) {
                    listSwitch.getListener().onChange(b);
                }
            });
        }

        @Override
        public int getItemCount() {
            return LIST_SWITCHES.size();
        }
    }
}
