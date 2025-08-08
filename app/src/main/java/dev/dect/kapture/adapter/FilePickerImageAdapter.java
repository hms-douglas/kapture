package dev.dect.kapture.adapter;

import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
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

public class FilePickerImageAdapter extends RecyclerView.Adapter<FilePickerImageAdapter.MyViewHolder> {
    public interface OnFilePickerImageAdapter {
        void onImageSelected(File folder);
    }

    private final ArrayList<File> IMAGES;

    private final OnFilePickerImageAdapter LISTENER;

    public FilePickerImageAdapter(ArrayList<File> images, OnFilePickerImageAdapter listener) {
        this.IMAGES = images;
        this.LISTENER = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout EL_CONTAINER;

        private final ImageView EL_PREVIEW;

        private final TextView EL_NAME;

        public MyViewHolder(View view) {
            super(view);

            this.EL_CONTAINER = view.findViewById(R.id.container);

            this.EL_NAME = view.findViewById(R.id.name);

            this.EL_PREVIEW = view.findViewById(R.id.preview);
        }
    }

    @NonNull
    @Override
    public FilePickerImageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_picker_file_image, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull FilePickerImageAdapter.MyViewHolder holder, int position) {
        final File image = IMAGES.get(position);

        holder.EL_NAME.setText(image.getName());

        holder.EL_PREVIEW.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));

        holder.EL_CONTAINER.setOnClickListener((v) -> {
           LISTENER.onImageSelected(IMAGES.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return IMAGES.size();
    }
}