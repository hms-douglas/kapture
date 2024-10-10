package dev.dect.kapture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.dect.kapture.R;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class ListButtonSubText {
    public interface OnListButtonSubText {
        void onButtonClicked(ListButtonSubText listButtonSubText);
    }

    private final int ID_TITLE;

    private String VALUE;

    private final boolean IS_LAST_FROM_GROUP;

    private final OnListButtonSubText LISTENER;

    public ListButtonSubText(int title, String value, OnListButtonSubText onListButtonSubText, boolean lastFromGroup) {
        this.ID_TITLE = title;
        this.VALUE = value;
        this.IS_LAST_FROM_GROUP = lastFromGroup;
        this.LISTENER = onListButtonSubText;
    }

    public ListButtonSubText(int title, int value, OnListButtonSubText onListButtonSubText, boolean lastFromGroup) {
        this.ID_TITLE = title;
        this.VALUE = String.valueOf(value);
        this.IS_LAST_FROM_GROUP = lastFromGroup;
        this.LISTENER = onListButtonSubText;
    }

    public int getIdTitle() {
        return ID_TITLE;
    }

    public String getValue() {
        return VALUE;
    }

    public void setValue(String v) {
        this.VALUE = v;
    }

    public void setValue(int v) {
        this.VALUE = String.valueOf(v);
    }

    public boolean isLastItemFromGroup(){
        return IS_LAST_FROM_GROUP;
    }

    private OnListButtonSubText getListener() {
        return LISTENER;
    }

    public static class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
        private final ArrayList<ListButtonSubText> LIST_BUTTONS;

        public Adapter(ArrayList<ListButtonSubText> listButtonSubTexts) {
            this.LIST_BUTTONS = listButtonSubTexts;
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            private final ConstraintLayout EL_CONTAINER;

            private final TextView EL_TITLE,
                                   EL_SUB_TITLE;

            public MyViewHolder(View view) {
                super(view);

                this.EL_CONTAINER = view.findViewById(R.id.listContainer);

                this.EL_TITLE = view.findViewById(R.id.itemTitle);
                this.EL_SUB_TITLE = view.findViewById(R.id.itemSubTitle);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_button_sub_text, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final ListButtonSubText listButtonSubText = LIST_BUTTONS.get(position);

            final Context ctx = holder.EL_CONTAINER.getContext();

            holder.EL_TITLE.setText(ctx.getString(listButtonSubText.getIdTitle()));

            holder.EL_SUB_TITLE.setText(listButtonSubText.getValue());

            if(!listButtonSubText.isLastItemFromGroup()) {
                holder.EL_CONTAINER.setBackgroundResource(R.drawable.list_item_divisor_horizontal_bottom);
            }

            holder.EL_CONTAINER.setOnClickListener((l) -> listButtonSubText.getListener().onButtonClicked(listButtonSubText));
        }

        @Override
        public int getItemCount() {
            return LIST_BUTTONS.size();
        }
    }
}
