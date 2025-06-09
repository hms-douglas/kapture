package dev.dect.kapture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.Utils;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class ListStorageItemAdapter extends RecyclerView.Adapter<ListStorageItemAdapter.MyViewHolder> {
    private final ArrayList<ListStorage.StorageItem> LIST_ITEMS;

    public ListStorageItemAdapter(ArrayList<ListStorage.StorageItem> listItems) {
        this.LIST_ITEMS = listItems;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView EL_COLOR;

        private final TextView EL_LABEL,
                               EL_VALUE;

        public MyViewHolder(View view) {
            super(view);

            this.EL_COLOR = view.findViewById(R.id.color);

            this.EL_LABEL = view.findViewById(R.id.label);

            this.EL_VALUE = view.findViewById(R.id.value);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_storage_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final ListStorage.StorageItem storageItem = LIST_ITEMS.get(position);

        final Context ctx = holder.EL_COLOR.getContext();

        holder.EL_LABEL.setText(storageItem.getLabelId());

        holder.EL_VALUE.setText(KFile.formatFileSize(storageItem.getValue()));

        Utils.drawColorSampleOnImageView(holder.EL_COLOR, ctx.getColor(storageItem.getColorId()));
    }

    @Override
    public int getItemCount() {
        return LIST_ITEMS.size();
    }
}

