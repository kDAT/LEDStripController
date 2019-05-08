package com.dat.simpleapp.ledstripcontroller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class ColorSelectorDialog extends DialogFragment {

    private Context mContext;
    private OnColorSelectedListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.color_picker, null);
        configView(view);

        builder.setTitle(R.string.color_dialog_title);
        builder.setView(view);

        return builder.create();
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int red, int green, int blue, int brightness);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mListener = listener;
    }

    private void configView(View view) {
        ImageView colorRed = view.findViewById(R.id.color_red);
        ImageView colorYellow = view.findViewById(R.id.color_yellow);
        ImageView colorGreen = view.findViewById(R.id.color_green);
        ImageView colorLightBlue = view.findViewById(R.id.color_light_blue);
        ImageView colorBlue = view.findViewById(R.id.color_blue);
        ImageView colorPink = view.findViewById(R.id.color_pink);
        ImageView colorWhite = view.findViewById(R.id.color_white);
        ImageView colorDat = view.findViewById(R.id.color_dat);
        ImageView colorPurple = view.findViewById(R.id.color_purple);

        colorRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onColorSelected(255, 0, 0, 255);
                dismiss();
            }
        });
        colorYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onColorSelected(255, 255, 0, 255);
                dismiss();
            }
        });
        colorGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onColorSelected(0, 255, 0, 255);
                dismiss();
            }
        });
        colorLightBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onColorSelected(0, 128, 255, 255);
                dismiss();
            }
        });
        colorBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onColorSelected(0, 0, 255, 255);
                dismiss();
            }
        });
        colorPink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onColorSelected(255, 0, 128, 255);
                dismiss();
            }
        });
        colorWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onColorSelected(255, 255, 255, 255);
                dismiss();
            }
        });
        colorDat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onColorSelected(253, 109, 7, 255);
                dismiss();
            }
        });
        colorPurple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onColorSelected(128, 0, 255, 255);
                dismiss();
            }
        });
    }
}
