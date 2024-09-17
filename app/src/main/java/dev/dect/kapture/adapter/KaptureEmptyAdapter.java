package dev.dect.kapture.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dev.dect.kapture.R;

public class KaptureEmptyAdapter extends RecyclerView.Adapter<KaptureEmptyAdapter.MyViewHolder> {
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(View view) {
            super(view);
        }
    }

    @NonNull
    @Override
    public KaptureEmptyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_kapture_empty, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull KaptureEmptyAdapter.MyViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() { return 1;}
}
