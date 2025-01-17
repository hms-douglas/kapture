package dev.dect.kapture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.utils.KProfile;
import dev.dect.kapture.utils.Utils;

@SuppressLint("SetTextI18n")
public class ProfileManagerAdapter extends RecyclerView.Adapter<ProfileManagerAdapter.MyViewHolder> {
    final private ArrayList<SharedPreferences> PROFILES;

    private RecyclerView RECYCLER_VIEW;

    public ProfileManagerAdapter(Context ctx) {
        this.PROFILES = KProfile.getAllProfilesSp(ctx);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout EL_CONTAINER;

        private final ImageButton EL_BUTTON_DELETE;

        private final ImageView EL_ICON;

        private final TextView EL_USER_NAME;

        public MyViewHolder(View view) {
            super(view);

            this.EL_CONTAINER = view.findViewById(R.id.container);

            this.EL_BUTTON_DELETE = view.findViewById(R.id.btnDelete);

            this.EL_ICON = view.findViewById(R.id.profileIcon);

            this.EL_USER_NAME = view.findViewById(R.id.profileName);
        }
    }

    @NonNull
    @Override
    public ProfileManagerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_profile_item_manage, parent, false));
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        this.RECYCLER_VIEW = recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileManagerAdapter.MyViewHolder holder, int position) {
        final Context ctx = holder.EL_USER_NAME.getContext();

        final SharedPreferences sp = PROFILES.get(holder.getAbsoluteAdapterPosition());

        holder.EL_USER_NAME.setText(sp.getString(Constants.Sp.Profile.SP_FILE_USER_NAME, "?"));

        final Drawable icon = Utils.getDrawable(ctx, KProfile.getIconResId(sp.getInt(Constants.Sp.Profile.SP_FILE_ICON, 0)));

        Utils.colorDrawable(icon, sp.getString(Constants.Sp.Profile.SP_FILE_ICON_COLOR, Utils.Converter.intColorToHex(ctx, R.color.app_bar_profile)));

        holder.EL_ICON.setImageDrawable(icon);

        holder.EL_CONTAINER.setOnClickListener((v) -> {
            final SharedPreferences sp2 = PROFILES.get(holder.getAbsoluteAdapterPosition());

            final String name = sp2.getString(Constants.Sp.Profile.SP_FILE_NAME, null);

            if(name == null) {
                Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();

                return;
            }

            KProfile.editProfile(ctx, name);
        });

        holder.EL_BUTTON_DELETE.setOnClickListener((v) -> {
            final SharedPreferences sp2 = PROFILES.get(holder.getAbsoluteAdapterPosition());

            final String name = sp2.getString(Constants.Sp.Profile.SP_FILE_NAME, null);

            if(name == null) {
                Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();

                return;
            }

            KProfile.deleteProfile(ctx, name, () -> RECYCLER_VIEW.callOnClick());
        });
    }

    @Override
    public int getItemCount() {
        return PROFILES.size();
    }
}