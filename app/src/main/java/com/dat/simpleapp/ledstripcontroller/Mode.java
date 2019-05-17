package com.dat.simpleapp.ledstripcontroller;

import android.util.Log;

public class Mode {

    private static final String TAG = "Mode";

    private byte[] mBytes;
    private BluetoothConnectionService mBluetoothConnectionService;

    public Mode(byte[] bytes, BluetoothConnectionService bluetoothConnectionService) {
        mBytes = bytes;
        mBluetoothConnectionService = bluetoothConnectionService;
    }

    public byte[] getBytes() {
        return mBytes;
    }

    public void setBytes(byte[] bytes, boolean send) {
        mBytes = bytes;
        if (send) mBluetoothConnectionService.write(bytes);
        Log.d(TAG, "setBytes: Mode:" + bytes[0] + "\tSend:" + send);
    }

    public void setStripSize(int stripSize) {
        mBytes[15] = (byte) stripSize;
    }
}
