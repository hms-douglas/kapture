package dev.dect.kapture.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.Utils;

public class PickerAppItemAdapter extends RecyclerView.Adapter<PickerAppItemAdapter.MyViewHolder> {
    private final ArrayList<String> SELECTED;

    private final boolean MULTIPLE;

    private final int MAX;

    private final Context CONTEXT;

    private final List<ApplicationInfo> APPS;

    private RecyclerView RECYCLER_VIEW;

    public PickerAppItemAdapter(Context ctx, int max, ArrayList<String> selected) {
        this.CONTEXT = ctx;
        this.MAX = max;
        this.SELECTED = selected;

        this.MULTIPLE = max != 1;

        this.APPS = Utils.Package.getAllAppsInfo(ctx, true);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout EL_CONTAINER;

        private final ImageView EL_CHECKBOX_RADIO,
                                EL_ICON;

        private final TextView EL_NAME;

        public MyViewHolder(View view) {
            super(view);

            this.EL_CONTAINER = view.findViewById(R.id.container);

            this.EL_NAME = view.findViewById(R.id.name);

            this.EL_CHECKBOX_RADIO = view.findViewById(R.id.radioCheckboxIcon);
            this.EL_ICON = view.findViewById(R.id.icon);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_picker_item_with_icon, parent, false)
        );
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        this.RECYCLER_VIEW = recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String packageName;

        final PackageManager pm = CONTEXT.getPackageManager();

        final ApplicationInfo ai = APPS.get(holder.getAbsoluteAdapterPosition());

        packageName = ai.packageName;

        holder.EL_NAME.setText(ai.loadLabel(pm));

        new Handler((Looper.getMainLooper())).post(() -> {
            try {
                holder.EL_ICON.setImageDrawable(CONTEXT.getPackageManager().getApplicationIcon(packageName));
            } catch (PackageManager.NameNotFoundException ignore) {
                holder.EL_ICON.setImageResource(R.drawable.icon_launch_app);
            }
        });

        if(MULTIPLE) {
            holder.EL_CHECKBOX_RADIO.setImageResource(SELECTED.contains(packageName) ? R.drawable.checkbox_on : R.drawable.checkbox_off);

            holder.EL_CONTAINER.setOnClickListener((v) -> {
                if(SELECTED.contains(packageName)) {
                   SELECTED.remove(packageName);
                } else if(SELECTED.size() < MAX) {
                    SELECTED.add(packageName);
                } else {
                    Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_error_select_app_max).replaceFirst("%d", String.valueOf(MAX)), Toast.LENGTH_SHORT).show();
                }

                notifyItemChanged(position);
            });
        } else {
            holder.EL_CHECKBOX_RADIO.setImageResource(SELECTED.contains(packageName) ? R.drawable.radio_on : R.drawable.radio_off);

            holder.EL_CONTAINER.setOnClickListener((v) -> {
                if(!SELECTED.contains(packageName)) {
                    SELECTED.set(0, packageName);

                    holder.EL_CHECKBOX_RADIO.setImageResource(R.drawable.radio_on);

                    RECYCLER_VIEW.callOnClick();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return APPS.size();
    }
}