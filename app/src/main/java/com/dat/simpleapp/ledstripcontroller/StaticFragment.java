package com.dat.simpleapp.ledstripcontroller;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.appyvet.materialrangebar.RangeBar;

public class StaticFragment extends Fragment {

    private static final String TAG = "StaticFragment";

    Context mContext;
    Mode mStaticMode;
    RangeBar rangeBar;
    RangeBar redBar;
    RangeBar greenBar;
    RangeBar blueBar;
    RangeBar brightnessBar;
    TextView rangeLeftText;
    TextView rangeRightText;
    private int leftPin;
    private int rightPin;
    private int colorRed;
    private int colorGreen;
    private int colorBlue;
    private int colorBrightness;
    private int mStripSize;

    public static final String DIALOG_TAG = "Color_Picker";

    public static StaticFragment newInstance(Mode staticMode) {
        StaticFragment fragment = new StaticFragment();
        fragment.setMode(staticMode);
        return fragment;
    }

    public void setMode(Mode staticMode) {
        mStaticMode = staticMode;
    }

    public void setStripSize(int stripSize) {
        mStripSize = stripSize;
        rangeBar.setTickEnd(mStripSize);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_static, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRangeBar(view);
        setupColorBar(view);
    }

    private void setupRangeBar(View view) {
        rangeBar = view.findViewById(R.id.static_range);
        rangeLeftText = view.findViewById(R.id.range_left_text);
        rangeRightText = view.findViewById(R.id.range_right_text);
        if (mStaticMode != null) {
            // sets the bar and the textViews
            mStripSize = mStaticMode.getBytes()[15] & 0xff;
            rangeBar.setTickStart(1);
            rangeBar.setTickEnd(mStripSize);
            leftPin = mStaticMode.getBytes()[2] & 0xff;
            rightPin = mStaticMode.getBytes()[3] & 0xff;
            rangeBar.setRangePinsByValue(leftPin, rightPin);
            rangeLeftText.setText(String.valueOf(leftPin));
            rangeRightText.setText(String.valueOf(rightPin));
        }
        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                leftPin = Integer.parseInt(leftPinValue);
                rightPin = Integer.parseInt(rightPinValue);
                updateRange();
            }
        });
        ImageView rangeLeftFirst = view.findViewById(R.id.range_left_first);
        rangeLeftFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftPin = 1;
                updateRange();
                rangeBar.setRangePinsByValue(leftPin, rightPin);
            }
        });
        ImageView rangeLeftPrev = view.findViewById(R.id.range_left_prev);
        rangeLeftPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leftPin > 1) leftPin--;
                updateRange();
                rangeBar.setRangePinsByValue(leftPin, rightPin);
            }
        });
        ImageView rangeLeftNext = view.findViewById(R.id.range_left_next);
        rangeLeftNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leftPin < rightPin) leftPin++;
                else if (leftPin < mStripSize) {
                    leftPin++;
                    rightPin++;
                }
                updateRange();
                rangeBar.setRangePinsByValue(leftPin, rightPin);
            }
        });
        ImageView rangeLeftLast = view.findViewById(R.id.range_left_last);
        rangeLeftLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leftPin < rightPin) leftPin = rightPin;
                else if (leftPin < mStripSize) {
                    leftPin++;
                    rightPin++;
                }
                updateRange();
                rangeBar.setRangePinsByValue(leftPin, rightPin);
            }
        });
        ImageView rangeRightFirst = view.findViewById(R.id.range_right_first);
        rangeRightFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rightPin > leftPin) rightPin = leftPin;
                else if (rightPin > 1) {
                    rightPin--;
                    leftPin--;
                }
                updateRange();
                rangeBar.setRangePinsByValue(leftPin, rightPin);
            }
        });
        ImageView rangeRightPrev = view.findViewById(R.id.range_right_prev);
        rangeRightPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rightPin > leftPin) rightPin--;
                else if (rightPin > 1) {
                    rightPin--;
                    leftPin--;
                }
                updateRange();
                rangeBar.setRangePinsByValue(leftPin, rightPin);
            }
        });
        ImageView rangeRightNext = view.findViewById(R.id.range_right_next);
        rangeRightNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rightPin < mStripSize) rightPin++;
                updateRange();
                rangeBar.setRangePinsByValue(leftPin, rightPin);
            }
        });
        ImageView rangeRightLast = view.findViewById(R.id.range_right_last);
        rangeRightLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rightPin = mStripSize;
                updateRange();
                rangeBar.setRangePinsByValue(leftPin, rightPin);
            }
        });
    }

    private void updateRange() {
        rangeLeftText.setText(String.valueOf(leftPin));
        rangeRightText.setText(String.valueOf(rightPin));
        if (mStaticMode != null) {
            // Update the Mode Object
            byte[] bytes = mStaticMode.getBytes();
            bytes[2] = (byte) leftPin;
            bytes[3] = (byte) rightPin;
            mStaticMode.setBytes(bytes, false);
        }
    }

    /**
     * Color bars
     */
    private void setupColorBar(View view) {
        redBar = view.findViewById(R.id.bar_red);
        greenBar = view.findViewById(R.id.bar_green);
        blueBar = view.findViewById(R.id.bar_blue);
        brightnessBar = view.findViewById(R.id.bar_brightness);
        if (mStaticMode != null) {
            colorRed = mStaticMode.getBytes()[4] & 0xff;
            colorGreen = mStaticMode.getBytes()[5] & 0xff;
            colorBlue = mStaticMode.getBytes()[6] & 0xff;
            colorBrightness = mStaticMode.getBytes()[1] & 0xff;
            redBar.setSeekPinByIndex(colorRed);
            greenBar.setSeekPinByIndex(colorGreen);
            blueBar.setSeekPinByIndex(colorBlue);
            brightnessBar.setSeekPinByIndex(colorBrightness);
        }

        redBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                colorRed = rightPinIndex;
                updateColor();
            }
        });
        greenBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                colorGreen = rightPinIndex;
                updateColor();
            }
        });
        blueBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                colorBlue = rightPinIndex;
                updateColor();
            }
        });
        brightnessBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                colorBrightness = rightPinIndex;
                updateColor();
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
                updateColor();
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
                updateColor();
            }
        });
    }

    private void updateColor() {
        if (mStaticMode != null) {
            byte[] bytes = mStaticMode.getBytes();
            bytes[4] = (byte) colorRed;
            bytes[5] = (byte) colorGreen;
            bytes[6] = (byte) colorBlue;
            bytes[1] = (byte) colorBrightness;
            mStaticMode.setBytes(bytes, true);
        }
    }
}
