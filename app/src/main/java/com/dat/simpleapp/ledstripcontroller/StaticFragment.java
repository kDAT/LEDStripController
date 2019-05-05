package com.dat.simpleapp.ledstripcontroller;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appyvet.materialrangebar.RangeBar;

public class StaticFragment extends Fragment {

    Context mContext;
    Mode mStaticMode;
    RangeBar rangeBar;
    TextView rangeLeftText;
    TextView rangeRightText;
    private int leftPin;
    private int rightPin;
    private int mStripSize;

    public static StaticFragment newInstance(Mode staticMode) {
        StaticFragment fragment = new StaticFragment();
        fragment.setMode(staticMode);
        return fragment;
    }

    public void setMode(Mode staticMode){
        mStaticMode = staticMode;
    }

    public void setStripSize(int stripSize){
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
    }

    private void setupRangeBar(View view){
        rangeBar = view.findViewById(R.id.static_range);
        rangeLeftText = view.findViewById(R.id.range_left_text);
        rangeRightText = view.findViewById(R.id.range_right_text);
        if (mStaticMode != null){
            // sets the bar and the textViews
            mStripSize = mStaticMode.getBytes()[15]&0xff;
            rangeBar.setTickStart(1);
            rangeBar.setTickEnd(mStripSize);
            leftPin = mStaticMode.getBytes()[2]&0xff;
            rightPin = mStaticMode.getBytes()[3]&0xff;
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
                else if (leftPin < mStripSize){
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
                else if (leftPin < mStripSize){
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

    private void updateRange(){
        rangeLeftText.setText(String.valueOf(leftPin));
        rangeRightText.setText(String.valueOf(rightPin));
        if (mStaticMode != null){
            // Update the Mode Object
            byte[] bytes = mStaticMode.getBytes();
            bytes[2] = (byte) leftPin;
            bytes[3] = (byte) rightPin;
            mStaticMode.setBytes(bytes, false);
        }
    }
}
