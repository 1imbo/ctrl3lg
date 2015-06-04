package com.itcms.onoff_simplifiedui.controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.View.OnClickListener;

import com.itcms.onoff_simplifiedui.R;

import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes.LFXPowerState;
import lifx.java.android.light.LFXLight;
import lifx.java.android.light.LFXLight.LFXLightListener;

/**
 * Created by Dmitriy G. on 03.06.2015.
 */
public class LightController implements OnCheckedChangeListener {

    private static final float BRIGHTNESS_MIN_VALUE = 0.0f;
    private static final float BRIGHTNESS_MAX_VALUE = 1.0f;
    private static final int BRIGHTNESS_BAR_SUPPOSED_MAX_VALUE = 100;
    private static final int BRIGHTNESS_ADJUSTMENT_STEP = 1;
    private static final int COLOR_TEMP_MIN_VALUE = 0;
    private static final int COLOR_TEMP_MAX_VALUE = 10000;
    private static final int COLOR_TEMP_BAR_SUPPOSED_MAX_VALUE = 100;
    private static final int COLOR_TEMP_ADJUSTMENT_STEP = 1;
    private static final String DIALOG_TAG = "EDIT_DIALOG";
    private static final String CURRENT_LABEL_KEY = "CURRENT_LABEL";

    private Context context;
    private LFXLight lfxLight;
    private Switch lightSwitch;
    private VerticalSeekBar lightDimSeek;
    private VerticalSeekBar lightTempSeek;
    private ImageButton lightLabelChange;
    private float brightness = 0.5000f;
    private int kelvin = 6500;

    public LightController(
            Context aContext,
            LFXLight aLfxLight,
            Switch aLightSwitch,
            VerticalSeekBar aLightDimSeek,
            VerticalSeekBar aLightTempSeek,
            ImageButton aLightLabelChange) {

        context = aContext;
        lfxLight = aLfxLight;
        lightSwitch = aLightSwitch;
        lightDimSeek = aLightDimSeek;
        lightTempSeek = aLightTempSeek;
        lightLabelChange = aLightLabelChange;

        int maxDimSeeker = BRIGHTNESS_BAR_SUPPOSED_MAX_VALUE / BRIGHTNESS_ADJUSTMENT_STEP *
                BRIGHTNESS_ADJUSTMENT_STEP;
        int maxTempSeeker = COLOR_TEMP_BAR_SUPPOSED_MAX_VALUE / COLOR_TEMP_ADJUSTMENT_STEP *
                COLOR_TEMP_ADJUSTMENT_STEP;

        lightDimSeek.setMax(maxDimSeeker);
        lightTempSeek.setMax(maxTempSeeker);

        lfxLight.addLightListener((LFXLightListener) context);
        lightSwitch.setOnCheckedChangeListener(this);
        lightDimSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setLightBrightness((float) progress / 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        lightTempSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setLightKelvin(progress * 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        lightLabelChange.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                final String currentLabel = lfxLight.getLabel();
                final Activity hostActivity = (Activity) context;

                AlertDialog.Builder builder = new AlertDialog.Builder(hostActivity);

                final LayoutInflater inflater = hostActivity.getLayoutInflater();

                View dialogLayout = inflater.inflate(R.layout.edit_label_dialog, null);
                TextView enterLabel = (TextView) dialogLayout.findViewById(R.id.new_label);
                final EditText editLabelText = (EditText) dialogLayout.findViewById(R.id.edit_label);

                enterLabel.setText(hostActivity.getString(R.string.edit_label_dialog_enter_label) + " " + currentLabel);

                editLabelText.setText(currentLabel);

                DialogInterface.OnClickListener dialogButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            String newLabel = editLabelText.getText().toString();
                            if (!currentLabel.equals(newLabel) && !newLabel.equals("")) {
                                lfxLight.setLabel(newLabel);
                            } else {
                                Toast.makeText(context, hostActivity.getString(R.string.label_no_changed), Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        } else {
                            dialog.dismiss();
                        }
                    }
                };

                builder.setTitle(R.string.edit_label_dialog_title)
                       .setView(dialogLayout)
                       .setPositiveButton(R.string.edit_label_dialog_ok, dialogButtonClickListener)
                       .setNegativeButton(R.string.edit_label_dialog_cancel, dialogButtonClickListener);

                builder.create().show();

            }
        });

        initSwitchWithOffPowerState();
    }

    private void initSwitchWithOffPowerState() {
        lfxLight.setPowerState(LFXPowerState.OFF);
        lightSwitch.setChecked(lfxLight.getPowerState() == LFXPowerState.ON);
    }

    private void setLightBrightness(float aBrightness) {

        if (aBrightness < BRIGHTNESS_MIN_VALUE) {
            brightness = BRIGHTNESS_MIN_VALUE;
        } else if (aBrightness > BRIGHTNESS_MAX_VALUE) {
            brightness = BRIGHTNESS_MAX_VALUE;
        } else {
            brightness = aBrightness;
        }

        LFXHSBKColor color = LFXHSBKColor.getColor(0.0f, 0.0f, brightness, kelvin);

        lfxLight.setColor(color);
    }

    private void setLightKelvin(int aKelvin) {
        if (aKelvin < COLOR_TEMP_MIN_VALUE) {
            kelvin = COLOR_TEMP_MIN_VALUE;
        } else if (aKelvin > COLOR_TEMP_MAX_VALUE) {
            kelvin = COLOR_TEMP_MAX_VALUE;
        } else {
            kelvin = aKelvin;
        }

        LFXHSBKColor color = LFXHSBKColor.getColor(0.0f, 0.0f, brightness, kelvin);

        lfxLight.setColor(color);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        lfxLight.setPowerState(isChecked ? LFXPowerState.ON : LFXPowerState.OFF);
    }

}
