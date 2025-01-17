package dev.dect.kapture.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.KProfile;
import dev.dect.kapture.utils.Utils;

@SuppressLint("SetTextI18n")
public class ProfileIconAdapter extends RecyclerView.Adapter<ProfileIconAdapter.MyViewHolder> {
    final private int[] ICONS;

    private int INDEX_ACTIVE;

    private RecyclerView RECYCLER_VIEW;

    public ProfileIconAdapter(int indexActive) {
        this.ICONS = KProfile.getAllIcons();
        this.INDEX_ACTIVE = indexActive;
    }

    public int getSelected() {
        return INDEX_ACTIVE;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton EL_BUTTON;

        public MyViewHolder(View view) {
            super(view);

            this.EL_BUTTON = view.findViewById(R.id.btnIcon);
        }
    }

    @NonNull
    @Override
    public ProfileIconAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_profile_icon_item, parent, false));
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        this.RECYCLER_VIEW = recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileIconAdapter.MyViewHolder holder, int position) {
        holder.EL_BUTTON.setImageResource(ICONS[position]);

        holder.EL_BUTTON.setForeground(Utils.getDrawable(holder.EL_BUTTON.getContext(), position == INDEX_ACTIVE ? R.drawable.btn_profile_icon_selected : R.drawable.btn_effect));

        holder.EL_BUTTON.setOnClickListener((v) -> {
            final int i = INDEX_ACTIVE;

            INDEX_ACTIVE = holder.getAbsoluteAdapterPosition();

            notifyItemChanged(i);
            notifyItemChanged(INDEX_ACTIVE);

            RECYCLER_VIEW.callOnClick();
        });
    }

    @Override
    public int getItemCount() {
        return ICONS.length;
    }
}