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

public class FilePickerFolderAdapter extends RecyclerView.Adapter<FilePickerFolderAdapter.MyViewHolder> {
    public interface OnFilePickerFolderAdapter {
        void onFolderClicked(File folder);
    }

    private final ArrayList<File> FOLDERS;

    private final OnFilePickerFolderAdapter LISTENER;

    public FilePickerFolderAdapter(ArrayList<File> folders, OnFilePickerFolderAdapter listener) {
        this.FOLDERS = folders;
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
    public FilePickerFolderAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_picker_file_folder, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull FilePickerFolderAdapter.MyViewHolder holder, int position) {
        final File folder = FOLDERS.get(position);

        holder.EL_NAME.setText(folder.getName());

        holder.EL_ICON.setImageResource(folder.isHidden() ? R.drawable.icon_folder_hidden : R.drawable.icon_folder);

        holder.EL_CONTAINER.setOnClickListener((v) -> {
           LISTENER.onFolderClicked(FOLDERS.get(holder.getAbsoluteAdapterPosition()));
        });
    }

    @Override
    public int getItemCount() {
        return FOLDERS.size();
    }
}