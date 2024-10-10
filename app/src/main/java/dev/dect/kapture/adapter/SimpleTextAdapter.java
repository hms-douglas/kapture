package dev.dect.kapture.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dev.dect.kapture.R;

public class SimpleTextAdapter extends RecyclerView.Adapter<SimpleTextAdapter.MyViewHolder> {
    private final String TEXT;

    private final int GRAVITY;

    public SimpleTextAdapter(String text, int gravity) {
        this.TEXT = text;
        this.GRAVITY = gravity;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView EL_TEXT;

        public MyViewHolder(View view) {
            super(view);

            this.EL_TEXT = view.findViewById(R.id.text);
        }
    }

    @NonNull
    @Override
    public SimpleTextAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SimpleTextAdapter.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_simple_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleTextAdapter.MyViewHolder holder, int position) {
        holder.EL_TEXT.setText(TEXT);
        holder.EL_TEXT.setGravity(GRAVITY);
    }

    @Override
    public int getItemCount() { return 1;}
}