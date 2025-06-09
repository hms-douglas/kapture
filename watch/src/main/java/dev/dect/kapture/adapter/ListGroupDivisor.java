package dev.dect.kapture.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dev.dect.kapture.R;

public class ListGroupDivisor {
    public static final int NO_TITLE = -1;

    private final int ID_TITLE;

    public ListGroupDivisor(int title) {
        this.ID_TITLE = title;
    }

    public int getIdTitle() {
        return ID_TITLE;
    }

    public boolean hasTitle() {
        return this.getIdTitle() != NO_TITLE;
    }

    public static class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
        private final ListGroupDivisor LIST_GROUP;

        public Adapter(ListGroupDivisor listGroup) {
            this.LIST_GROUP = listGroup;
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            private final TextView EL_TITLE;

            public MyViewHolder(View view) {
                super(view);

                this.EL_TITLE = view.findViewById(R.id.groupTitle);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_group_divider, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            if(LIST_GROUP.hasTitle()) {
                holder.EL_TITLE.setText(holder.EL_TITLE.getContext().getString(LIST_GROUP.getIdTitle()));
            } else {
                holder.EL_TITLE.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }
}
