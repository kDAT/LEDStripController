package com.dat.simpleapp.ledstripcontroller;

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
    }

    public void setStripSize(int stripSize){
        mBytes[15] = (byte) stripSize;
    }
}
