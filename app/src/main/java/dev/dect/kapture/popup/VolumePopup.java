package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.slider.Slider;

import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.Utils;

@SuppressLint({"InflateParams", "SetTextI18n"})
public class VolumePopup extends Dialog {
    public interface OnVolumePopupListener {
        default void onChanging(int min, int max, int volumePercentage) {}
        default void onChanged(int min, int max, int volumePercentage) {}
    }

    private final ConstraintLayout POPUP_VIEW,
                                   POPUP_CONTAINER;

    public VolumePopup(Context ctx, int type, int volumePercentage, int btnYes, OnVolumePopupListener l) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_volume, null);

        this.POPUP_CONTAINER = view.findViewById(R.id.popupContainer);
        this.POPUP_VIEW = view.findViewById(R.id.popup);

        this.POPUP_CONTAINER.setOnClickListener((v) -> this.dismissWithAnimation());

        final AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

        final int min = audioManager.getStreamMinVolume(type),
                  max = audioManager.getStreamMaxVolume(type);

        final TextView title = view.findViewById(R.id.popupTitle),
                       progress = view.findViewById(R.id.popupProgress);


        final ImageView icon = view.findViewById(R.id.popupIcon);

        switch(type) {
            case AudioManager.STREAM_MUSIC:
            default:
                title.setText(R.string.popup_media_volume);
                icon.setImageResource(R.drawable.icon_volume_media);
                break;
        }

        final Slider volumeSlider = view.findViewById(R.id.popupSlider);

        volumeSlider.setValueFrom(min);
        volumeSlider.setValueTo(max);

        if(volumePercentage == -1) {
            volumeSlider.setValue(audioManager.getStreamVolume(type));
        } else {
            volumeSlider.setValue((float) (volumePercentage * max) / 100f);
        }

        progress.setText(((int)((volumeSlider.getValue()) * 100 / max)) + "%");

        volumeSlider.addOnChangeListener((slider, value, fromUser) -> {
            final int percentage = ((int)((value) * 100 / max));

            if(fromUser) {
                l.onChanging(min, max,  percentage);
            }

            progress.setText(percentage+ "%");
        });

        final AppCompatButton btnMain = view.findViewById(R.id.popupBtnYes);

        btnMain.setText(btnYes);

        btnMain.setOnClickListener((v) -> {
            l.onChanged(min, max, ((int)((volumeSlider.getValue() * 100 / max))));

            this.dismissWithAnimation();
        });

        Utils.Popup.setMaxHeight(ctx, view.findViewById(R.id.popup));

        this.setContentView(view);

        Objects.requireNonNull(getWindow()).setStatusBarColor(ctx.getColor(R.color.popup_background_transparent));
    }

    public void dismissWithAnimation() {
        Utils.Popup.callOutAnimation(this, POPUP_CONTAINER, POPUP_VIEW);
    }
}
