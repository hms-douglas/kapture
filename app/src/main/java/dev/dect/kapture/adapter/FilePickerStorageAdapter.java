package dev.dect.kapture.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import dev.dect.kapture.R;

public class FilePickerStorageAdapter extends RecyclerView.Adapter<FilePickerStorageAdapter.MyViewHolder> {
    public interface OnFilePickerStorageAdapter {
        void onStorageClicked(File root);
    }

    private final ArrayList<File> STORAGES;

    private final OnFilePickerStorageAdapter LISTENER;

    public FilePickerStorageAdapter(ArrayList<File> storages, OnFilePickerStorageAdapter listener) {
        this.STORAGES = storages;
        this.LISTENER = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout EL_CONTAINER;

        private final ImageView EL_ICON;

        private final TextView EL_NAME;

        public MyViewHolder(View view) {
            super(view);

            this.EL_CONTAINER = view.findViewById(R.id.container);

            this.EL_NAME = view.findViewById(R.id.name);

            this.EL_ICON = view.findViewById(R.id.icon);
        }
    }

    @NonNull
    @Override
    public FilePickerStorageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_picker_file_storage, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull FilePickerStorageAdapter.MyViewHolder holder, int position) {
        switch(STORAGES.get(position).getName()) {
            case "0":
                holder.EL_NAME.setText(R.string.internal_storage);
                holder.EL_ICON.setImageResource(R.drawable.icon_storage_internal);
                break;

            case "95":
                holder.EL_NAME.setText(R.string.dual_storage);
                holder.EL_ICON.setImageResource(R.drawable.icon_storage_dual);
                break;

            default:
                holder.EL_NAME.setText(R.string.sd_card);
                holder.EL_ICON.setImageResource(R.drawable.icon_storage_sd);
                break;
        }

        holder.EL_CONTAINER.setOnClickListener((v) -> {
           LISTENER.onStorageClicked(STORAGES.get(holder.getAbsoluteAdapterPosition()));
        });
    }

    @Override
    public int getItemCount() {
        return STORAGES.size();
    }
}