package com.dat.simpleapp.ledstripcontroller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import java.util.ArrayList;

public class DynamicFragment extends Fragment {

    Context mContext;
    Mode mDynamicMode;
    ArrayList<Drawable> waveDrawables;
    private int waveType = 0; // 0-6
    private int waveInverted = 0; // 0 or 7
    private int waveInvBool = 0; // 0-1

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
        waveLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waveType == 0) waveType = 6;
                else waveType--;
                waveImage.setImageDrawable(waveDrawables.get(waveType + waveInverted));
                updateMode();
            }
        });
        ImageView waveRight = view.findViewById(R.id.wave_right);
        waveRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waveType == 6) waveType = 0;
                else waveType++;
                waveImage.setImageDrawable(waveDrawables.get(waveType + waveInverted));
                updateMode();
            }
        });
        Switch waveInvertSwitch = view.findViewById(R.id.wave_invert);
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

    private void updateMode() {
        if (mDynamicMode != null) {
            byte[] bytes = mDynamicMode.getBytes();
            bytes[3] = (byte) waveType;
            bytes[5] = (byte) waveInvBool;
            mDynamicMode.setBytes(bytes, true);
        }
    }
}
