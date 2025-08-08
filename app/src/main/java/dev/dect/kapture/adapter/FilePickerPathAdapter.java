package dev.dect.kapture.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Environment;
import android.util.Log;
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
import java.util.Collections;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;

public class FilePickerPathAdapter extends RecyclerView.Adapter<FilePickerPathAdapter.MyViewHolder> {
    public interface OnFilePickerPathAdapter {
        void onPillClicked(File folder);
    }

    private final ArrayList<File> FOLDERS = new ArrayList<>();

    private final OnFilePickerPathAdapter LISTENER;

    public FilePickerPathAdapter(File folder, ArrayList<File> roots, OnFilePickerPathAdapter listener) {
        this.LISTENER = listener;

        final ArrayList<String> rootsPaths = new ArrayList<>();

        for(File f : roots) {
            rootsPaths.add(f.getAbsolutePath());
        }

        while(folder != null && !rootsPaths.contains(folder.getAbsolutePath())) {
            FOLDERS.add(folder);

            folder = folder.getParentFile();
        }

        FOLDERS.add(folder);

        Collections.reverse(FOLDERS);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout EL_CONTAINER;

        private final ImageView EL_ICON;

        private final TextView EL_NAME;

        public MyViewHolder(View view) {
            super(view);

            this.EL_CONTAINER = view.findViewById(R.id.container);

            this.EL_ICON = view.findViewById(R.id.icon);

            this.EL_NAME = view.findViewById(R.id.name);
        }
    }

    @NonNull
    @Override
    public FilePickerPathAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_picker_file_pill, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull FilePickerPathAdapter.MyViewHolder holder, int position) {
        final File folder = FOLDERS.get(position);

        if(position == 0) {
            switch(folder.getName()) {
                case "0":
                    holder.EL_NAME.setText(R.string.internal_storage);
                    break;

                case "95":
                    holder.EL_NAME.setText(R.string.dual_storage);
                    break;

                default:
                    holder.EL_NAME.setText(R.string.sd_card);
                    break;
            }
        } else {
            holder.EL_NAME.setText(folder.getName());
        }

        holder.EL_CONTAINER.setOnClickListener((v) -> {
           LISTENER.onPillClicked(FOLDERS.get(holder.getAbsoluteAdapterPosition()));
        });

        final Context ctx = holder.EL_CONTAINER.getContext();

        if(position == (getItemCount() - 1)) {
            holder.EL_NAME.setTextColor(ctx.getColor(R.color.pill_text_active));
            holder.EL_ICON.setImageTintList(ColorStateList.valueOf(ctx.getColor(R.color.pill_icon_active)));
        }
    }

    @Override
    public int getItemCount() {
        return FOLDERS.size();
    }
}