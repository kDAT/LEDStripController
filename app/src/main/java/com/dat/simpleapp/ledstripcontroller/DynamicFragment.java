package com.dat.simpleapp.ledstripcontroller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.appyvet.materialrangebar.RangeBar;

import java.util.ArrayList;

public class DynamicFragment extends Fragment {

    Context mContext;
    Mode mDynamicMode;
    RangeBar redBar;
    RangeBar greenBar;
    RangeBar blueBar;
    RangeBar brightnessBar;
    RangeBar speedBar;
    RangeBar sizeBar;
    ArrayList<Drawable> waveDrawables;
    private int waveType = 0; // 0-6
    private int waveInverted = 0; // 0 or 7
    private int waveInvBool = 0; // 0-1
    private int colorRed;
    private int colorGreen;
    private int colorBlue;
    private int colorBrightness;
    private int colorSpeed;
    private int colorSize;

    public static final String DIALOG_TAG = "Color_Picker";

    public static DynamicFragment newInstance(Mode dynamicMode) {
        DynamicFragment fragment = new DynamicFragment();
        fragment.setMode(dynamicMode);
        return fragment;
    }

    public void setMode(Mode dynamicMode) {
        mDynamicMode = dynamicMode;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dynamic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupWaveType(view);
        setupColorBar(view);
    }

    private void setupWaveType(View view) {
        TypedArray array = getResources().obtainTypedArray(R.array.waves);
        waveDrawables = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            waveDrawables.add(array.getDrawable(i));
        }
        array.recycle();

        final ImageView waveImage = view.findViewById(R.id.wave_type);
        ImageView waveLeft = view.findViewById(R.id.wave_left);
        ImageView waveRight = view.findViewById(R.id.wave_right);
        Switch waveInvertSwitch = view.findViewById(R.id.wave_invert);
        if (mDynamicMode != null) {
            waveType = mDynamicMode.getBytes()[3] & 0xff;
            waveInvBool = mDynamicMode.getBytes()[5] & 0xff;
            if (waveInvBool == 1) {
                waveInverted = 7;
                waveInvertSwitch.setChecked(true);
            } else {
                waveInverted = 0;
                waveInvertSwitch.setChecked(false);
            }
            waveImage.setImageDrawable(waveDrawables.get(waveType + waveInverted));
        }
        waveLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waveType == 0) waveType = 6;
                else waveType--;
                waveImage.setImageDrawable(waveDrawables.get(waveType + waveInverted));
                updateMode();
            }
        });
        waveRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waveType == 6) waveType = 0;
                else waveType++;
                waveImage.setImageDrawable(waveDrawables.get(waveType + waveInverted));
                updateMode();
            }
        });
        waveInvertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    waveInvBool = 1;
                    waveInverted = 7;
                } else {
                    waveInvBool = 0;
                    waveInverted = 0;
                }
                waveImage.setImageDrawable(waveDrawables.get(waveType + waveInverted));
                updateMode();
            }
        });
    }

    private void setupColorBar(View view) {
        redBar = view.findViewById(R.id.bar_red);
        greenBar = view.findViewById(R.id.bar_green);
        blueBar = view.findViewById(R.id.bar_blue);
        brightnessBar = view.findViewById(R.id.bar_brightness);
        speedBar = view.findViewById(R.id.bar_speed);
        sizeBar = view.findViewById(R.id.bar_size);
        if (mDynamicMode != null) {
            colorRed = mDynamicMode.getBytes()[6] & 0xff;
            colorGreen = mDynamicMode.getBytes()[7] & 0xff;
            colorBlue = mDynamicMode.getBytes()[8] & 0xff;
            colorBrightness = mDynamicMode.getBytes()[1] & 0xff;
            colorSpeed = mDynamicMode.getBytes()[2] & 0xff;
            colorSize = mDynamicMode.getBytes()[4] & 0xff;
            redBar.setSeekPinByIndex(colorRed);
            greenBar.setSeekPinByIndex(colorGreen);
            blueBar.setSeekPinByIndex(colorBlue);
            brightnessBar.setSeekPinByIndex(colorBrightness);
            speedBar.setSeekPinByIndex(colorSpeed);
            sizeBar.setSeekPinByIndex(colorSize);
        }
        redBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                colorRed = rightPinIndex;
                updateMode();
            }
        });
        greenBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                colorGreen = rightPinIndex;
                updateMode();
            }
        });
        blueBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                colorBlue = rightPinIndex;
                updateMode();
            }
        });
        brightnessBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                colorBrightness = rightPinIndex;
                updateMode();
            }
        });
        speedBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                colorSpeed = rightPinIndex;
                updateMode();
            }
        });
        sizeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                colorSize = rightPinIndex;
                updateMode();
            }
        });

        Button buttonClear = view.findViewById(R.id.button_clear);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorRed = 0;
                colorGreen = 0;
                colorBlue = 0;
                colorBrightness = 255;
                redBar.setSeekPinByIndex(colorRed);
                greenBar.setSeekPinByIndex(colorGreen);
                blueBar.setSeekPinByIndex(colorBlue);
                brightnessBar.setSeekPinByIndex(colorBrightness);
                updateMode();
            }
        });

        final ColorSelectorDialog colorSelectorDialog = new ColorSelectorDialog();
        Button colorSelector = view.findViewById(R.id.button_select_color);
        colorSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorSelectorDialog.show(getChildFragmentManager(), DIALOG_TAG);
            }
        });
        colorSelectorDialog.setOnColorSelectedListener(new ColorSelectorDialog.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int red, int green, int blue, int brightness) {
                colorRed = red;
                colorGreen = green;
                colorBlue = blue;
                colorBrightness = brightness;
                redBar.setSeekPinByIndex(colorRed);
                greenBar.setSeekPinByIndex(colorGreen);
                blueBar.setSeekPinByIndex(colorBlue);
                brightnessBar.setSeekPinByIndex(colorBrightness);
                updateMode();
            }
        });
    }

    private void updateMode() {
        if (mDynamicMode != null) {
            byte[] bytes = mDynamicMode.getBytes();
            bytes[1] = (byte) colorBrightness;
            bytes[2] = (byte) colorSpeed;
            bytes[3] = (byte) waveType;
            bytes[4] = (byte) colorSize;
            bytes[5] = (byte) waveInvBool;
            bytes[6] = (byte) colorRed;
            bytes[7] = (byte) colorGreen;
            bytes[8] = (byte) colorBlue;
            mDynamicMode.setBytes(bytes, true);
        }
    }
}
