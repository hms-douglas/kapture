package dev.dect.kapture.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import dev.dect.kapture.R;
import dev.dect.kapture.data.KSettings;

public class PickerFontItemAdapter extends RecyclerView.Adapter<PickerFontItemAdapter.MyViewHolder> {
    private final String[] DISPLAY_NAMES,
                           PATHS;

    private int INDEX_ACTIVE;

    private RecyclerView RECYCLER_VIEW;

    public PickerFontItemAdapter(String[] displayNames, String[] paths, int indexActive) {
        this.DISPLAY_NAMES = displayNames;
        this.PATHS = paths;
        this.INDEX_ACTIVE = indexActive;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout EL_CONTAINER;

        private final ImageView EL_RADIO;

        private final TextView EL_NAME;

        public MyViewHolder(View view) {
            super(view);

            this.EL_CONTAINER = view.findViewById(R.id.radioContainer);

            this.EL_NAME = view.findViewById(R.id.radioName);

            this.EL_RADIO = view.findViewById(R.id.radioIcon);
        }
    }

    @NonNull
    @Override
    public PickerFontItemAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PickerFontItemAdapter.MyViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_picker_item, parent, false)
        );
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        this.RECYCLER_VIEW = recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull PickerFontItemAdapter.MyViewHolder holder, int position) {
        holder.EL_NAME.setText(DISPLAY_NAMES[position]);

        holder.EL_NAME.setTypeface(KSettings.getTypeFaceForFontPath(holder.EL_NAME.getContext(), PATHS[position]));

        holder.EL_RADIO.setImageResource(position == INDEX_ACTIVE ? R.drawable.radio_on : R.drawable.radio_off);

        if(DISPLAY_NAMES.length != 1) {
            holder.EL_CONTAINER.setOnClickListener((l) -> {
                holder.EL_RADIO.setImageResource(R.drawable.radio_on);

                final int i = INDEX_ACTIVE;

                INDEX_ACTIVE = holder.getAbsoluteAdapterPosition();

                notifyItemChanged(i);

                RECYCLER_VIEW.callOnClick();
            });
        }
    }

    @Override
    public int getItemCount() {
        return DISPLAY_NAMES.length;
    }

    public int getSelected() {
        return INDEX_ACTIVE;
    }
}