package dev.dect.kapture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.MainActivity;
import dev.dect.kapture.data.DB;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.popup.DialogPopup;
import dev.dect.kapture.popup.ExtraPopup;
import dev.dect.kapture.utils.KFile;

/** @noinspection ResultOfMethodCallIgnored*/
@SuppressLint("SetTextI18n")
public class ExtraItemAdapter extends RecyclerView.Adapter<ExtraItemAdapter.MyViewHolder> {
    private final ArrayList<Kapture.Extra> EXTRAS;

    private final ExtraPopup POPUP;

    public ExtraItemAdapter(ArrayList<Kapture.Extra> extras, @Nullable ExtraPopup popup) {
        this.EXTRAS = extras;
        this.POPUP = popup;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatButton EL_BTN_PLAY,
                                      EL_BTN_SHARE,
                                      EL_BTN_DELETE;

        private final TextView EL_NAME;

        public MyViewHolder(View view) {
            super(view);

            this.EL_BTN_PLAY = view.findViewById(R.id.btnPlay);
            this.EL_BTN_SHARE = view.findViewById(R.id.btnShare);
            this.EL_BTN_DELETE = view.findViewById(R.id.btnDelete);

            this.EL_NAME = view.findViewById(R.id.extraName);
        }
    }

    @NonNull
    @Override
    public ExtraItemAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_extra_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExtraItemAdapter.MyViewHolder holder, int position) {
        final Context ctx = holder.EL_BTN_PLAY.getContext();

        final Kapture.Extra extra = EXTRAS.get(position);

        holder.EL_BTN_PLAY.setOnClickListener((v) -> KFile.openFile(ctx, extra.getLocation()));

        holder.EL_BTN_SHARE.setOnClickListener((v) -> KFile.shareFile(ctx, extra.getLocation()));

        holder.EL_BTN_DELETE.setOnClickListener((v) -> {
            final File f = new File(extra.getLocation());

            new DialogPopup(
                ctx,
                DialogPopup.NO_TEXT,
                ctx.getString(R.string.popup_delete_text_1) + " " + f.getName() + "?",
                R.string.popup_btn_delete,
                () -> {
                    new DB(ctx).deleteExtra(extra);

                    f.delete();

                    try{
                        this.EXTRAS.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, EXTRAS.size());
                    } catch (Exception ignore){}

                    if(EXTRAS.isEmpty()) {
                        Objects.requireNonNull(POPUP).dismissWithAnimation();

                        try {
                            if(MainActivity.getInstance() != null) {
                                MainActivity.getInstance().getKaptureFragment().triggerRefresh();
                            }
                        } catch (Exception ignore) {}
                    }
                },
                R.string.popup_btn_cancel,
                null,
                false,
                false,
                true
            ).show();
        });

        holder.EL_NAME.setText(extra.getTypeName(ctx));
    }

    @Override
    public int getItemCount() {
        return EXTRAS.size();
    }
}