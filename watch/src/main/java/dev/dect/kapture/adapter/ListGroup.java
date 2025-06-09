package dev.dect.kapture.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dev.dect.kapture.R;

public class ListGroup {
    public static final int NO_TITLE = -1;

    private final int ID_TITLE;

    private final ConcatAdapter CONCAT_ADAPTER;

    public ListGroup(int title, ConcatAdapter concatAdapter) {
        this.ID_TITLE = title;
        this.CONCAT_ADAPTER = concatAdapter;
    }

    public int getIdTitle() {
        return ID_TITLE;
    }

    public ConcatAdapter getAdapter() {
        return CONCAT_ADAPTER;
    }

    public boolean hasTitle() {
        return this.getIdTitle() != NO_TITLE;
    }

    public static class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
        private final ListGroup LIST_GROUP;

        public Adapter(ListGroup listGroup) {
            this.LIST_GROUP = listGroup;
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            private final RecyclerView EL_RECYCLER_VIEW;

            private final TextView EL_TITLE;

            public MyViewHolder(View view) {
                super(view);

                this.EL_RECYCLER_VIEW = view.findViewById(R.id.recyclerView);

                this.EL_TITLE = view.findViewById(R.id.groupTitle);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_group, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Context ctx = holder.EL_RECYCLER_VIEW.getContext();

            if(LIST_GROUP.hasTitle()) {
                holder.EL_TITLE.setText(ctx.getString(LIST_GROUP.getIdTitle()));
            } else {
                holder.EL_TITLE.setVisibility(View.GONE);
            }

            holder.EL_RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(ctx));
            holder.EL_RECYCLER_VIEW.setNestedScrollingEnabled(false);
            holder.EL_RECYCLER_VIEW.setAdapter(LIST_GROUP.getAdapter());
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }
}
