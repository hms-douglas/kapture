package dev.dect.kapture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraCharacteristics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DefaultSettings;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.Utils;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class ListCameraSize {
    private final boolean IS_LAST_FROM_GROUP;

    public ListCameraSize(boolean lastFromGroup) {
        this.IS_LAST_FROM_GROUP = lastFromGroup;
    }

    public boolean isLastItemFromGroup(){
        return IS_LAST_FROM_GROUP;
    }

    public static class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
        private final ListCameraSize LIST_CAMERA_SIZE;

        public Adapter(ListCameraSize listCameraSize) {
            this.LIST_CAMERA_SIZE = listCameraSize;
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            private final ImageView EL_IMAGE_EXAMPLE;

            private final Slider EL_SLIDER;

            private final LinearLayout EL_CONTAINER;

            public MyViewHolder(View view) {
                super(view);

                this.EL_IMAGE_EXAMPLE = view.findViewById(R.id.example);

                this.EL_SLIDER = view.findViewById(R.id.size);

                this.EL_CONTAINER = view.findViewById(R.id.container);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_camera_size, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Context ctx = holder.EL_IMAGE_EXAMPLE.getContext();

            final SharedPreferences sp = ctx.getSharedPreferences(Constants.SP, Context.MODE_PRIVATE);

            final KSettings ks = new KSettings(ctx);

            holder.EL_IMAGE_EXAMPLE.setImageResource(ks.getCameraFacingLens() == CameraCharacteristics.LENS_FACING_FRONT ? R.drawable.camera_frame_example_front : R.drawable.camera_frame_example_back);

            holder.EL_IMAGE_EXAMPLE.setBackgroundResource(KSettings.getCameraShapeResourceExample(ks.getCameraShape()));

            if(!LIST_CAMERA_SIZE.isLastItemFromGroup()) {
                holder.EL_CONTAINER.setBackgroundResource(R.drawable.list_item_divisor_horizontal_bottom);
            }

            final float[] previousValue = new float[]{0};

            holder.EL_SLIDER.addOnChangeListener((slider, value, fromUser) -> {
                if(CapturingService.isRecording()) {
                    if(fromUser) {
                        holder.EL_SLIDER.setValue(previousValue[0]);
                        Toast.makeText(ctx, ctx.getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();
                    }

                    return;
                }

                previousValue[0] = value;

                final ViewGroup.LayoutParams layoutParams = holder.EL_IMAGE_EXAMPLE.getLayoutParams();

                layoutParams.height = Utils.Converter.dpToPx(ctx, value);

                holder.EL_IMAGE_EXAMPLE.setLayoutParams(layoutParams);

                sp.edit().putInt(Constants.SP_KEY_CAMERA_SIZE, (int) value).apply();
            });

            holder.EL_SLIDER.setValue(sp.getInt(Constants.SP_KEY_CAMERA_SIZE, DefaultSettings.CAMERA_SIZE));
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }
}
