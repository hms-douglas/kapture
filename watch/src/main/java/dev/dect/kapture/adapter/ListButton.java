package dev.dect.kapture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.service.CapturingService;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class ListButton {
    public interface OnListButtonSubText {
        void onButtonClicked();
    }

    public static final int NO_TEXT = -1;

    private final int ID_TITLE,
                      ID_SUB_TITLE;

    private final boolean IS_LAST_FROM_GROUP;

    private final OnListButtonSubText LISTENER;

    public ListButton(int title, int subTitle, OnListButtonSubText onListButtonSubText, boolean lastFromGroup) {
        this.ID_TITLE = title;
        this.ID_SUB_TITLE = subTitle;
        this.IS_LAST_FROM_GROUP = lastFromGroup;
        this.LISTENER = onListButtonSubText;
    }

    public int getIdTitle() {
        return ID_TITLE;
    }

    public int getIdSubTitle() {
        return ID_SUB_TITLE;
    }

    public boolean hasSubTitle() {
        return this.getIdSubTitle() != NO_TEXT;
    }

    public boolean isLastItemFromGroup(){
        return IS_LAST_FROM_GROUP;
    }

    private OnListButtonSubText getListener() {
        return LISTENER;
    }

    public static class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
        private final ArrayList<ListButton> LIST_BUTTONS;

        public Adapter(ArrayList<ListButton> listButtonSubTexts) {
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
                LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_button, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final ListButton listButton = LIST_BUTTONS.get(position);

            final Context ctx = holder.EL_CONTAINER.getContext();

            holder.EL_TITLE.setText(ctx.getString(listButton.getIdTitle()));

            if(listButton.hasSubTitle()) {
                holder.EL_SUB_TITLE.setText(listButton.getIdSubTitle());
            } else {
                holder.EL_SUB_TITLE.setVisibility(View.GONE);
            }

            if(!listButton.isLastItemFromGroup()) {
                holder.EL_CONTAINER.setBackgroundResource(R.drawable.list_item_divisor_horizontal_bottom);
            }

            holder.EL_CONTAINER.setOnClickListener((l) -> {
                if(CapturingService.isRecording()) {
                    Toast.makeText(ctx, ctx.getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

                    return;
                }

                listButton.getListener().onButtonClicked();
            });
        }

        @Override
        public int getItemCount() {
            return LIST_BUTTONS.size();
        }
    }
}
