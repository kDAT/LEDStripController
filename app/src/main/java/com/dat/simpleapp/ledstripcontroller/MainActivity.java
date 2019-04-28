package com.dat.simpleapp.ledstripcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.ParcelUuid;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    BluetoothAdapter mBluetoothAdapter;
    BluetoothConnectionService mBluetoothConnectionService;
    private ArrayList<BluetoothDevice> arrayPairedDevices;

    private int mDefaultStripSize = 30;
    private int mStripSize;
    public static final String MY_PREFS_FILE = "MyPfresFile";
    public static final String STRIP_SIZE = "StripSize";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Starting Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
        }
        //Sets a BroadcastReceiver for checking if Bluetooth is On or Off
        IntentFilter BTOnOff = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStateChange, BTOnOff);
        //BluetoothConnectionService
        mBluetoothConnectionService = new BluetoothConnectionService();
        setBluetoothConnectionServiceListeners();

        //Strip size with SharedPreferences
        SharedPreferences stripSize = getSharedPreferences(MY_PREFS_FILE, Context.MODE_PRIVATE);
        mStripSize = stripSize.getInt(STRIP_SIZE, mDefaultStripSize);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothStateChange);
    }

    /**
     * BluetoothConnectionService Listeners
     */
    private void setBluetoothConnectionServiceListeners() {
        mBluetoothConnectionService.setOnBluetoothConnectionListener(
                new BluetoothConnectionService.OnBluetoothConnectionListener() {
                    @Override
                    public void onMessageReceived(byte[] buffer, int bytes) {
                        //No need, not receiving data
                    }

                    @Override
                    public void onConnectionStateChanged(int state) {
                        if (state != BluetoothConnectionService.STATE_LISTEN) {
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onDeviceConnected(BluetoothDevice device) {
                        Toast.makeText(MainActivity.this, getString(R.string.btlistener_connected_to)
                                + " " + device.getName(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionFailed() {
                        Toast.makeText(MainActivity.this, R.string.btlistener_connection_failed, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionLost() {
                        Toast.makeText(MainActivity.this, R.string.btlistener_connection_lost, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Broadcast Receivers
     */
    private final BroadcastReceiver mBluetoothStateChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int extra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (extra) {
                    case BluetoothAdapter.STATE_ON:
                    case BluetoothAdapter.STATE_OFF:
                        //update menu
                        invalidateOptionsMenu();
                        break;
                }
            }
        }
    };

    /**
     * MENU Related stuff
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetooth_item:
                updateBluetooth();
                return true;
            case R.id.stripSize_item:
                updateStripSize();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem bluetoothItem = menu.findItem(R.id.bluetooth_item);
        if (mBluetoothAdapter.isEnabled()) {
            switch (mBluetoothConnectionService.getState()) {
                case BluetoothConnectionService.STATE_NONE:
                    bluetoothItem.setIcon(R.drawable.ic_bluetooth_white_24dp);
                    return true;
                case BluetoothConnectionService.STATE_CONNECTING:
                    bluetoothItem.setIcon(R.drawable.ic_bluetooth_searching_white_24dp);
                    return true;
                case BluetoothConnectionService.STATE_CONNECTED:
                    bluetoothItem.setIcon(R.drawable.ic_bluetooth_connected_white_24dp);
                    return true;
                default:
                    return super.onPrepareOptionsMenu(menu);
            }
        } else {
            bluetoothItem.setIcon(R.drawable.ic_bluetooth_disabled_white_24dp);
            return true;
        }
    }

    private void updateBluetooth() {
        if (mBluetoothAdapter.isEnabled()) {
            switch (mBluetoothConnectionService.getState()) {
                case BluetoothConnectionService.STATE_NONE:
                    connectBluetooth();
                    break;
                case BluetoothConnectionService.STATE_CONNECTING:
                case BluetoothConnectionService.STATE_CONNECTED:
                    mBluetoothConnectionService.stop();
                    break;
            }
        } else {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
        }
    }


    private void connectBluetooth() {
        final String[] names = getBonded();
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_select_one))
                .setItems(names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BluetoothDevice device = arrayPairedDevices.get(which);
                        ParcelUuid[] pUUID = device.getUuids();
                        if (pUUID != null) {
                            UUID uuidDevice = pUUID[0].getUuid();
                            mBluetoothConnectionService.connect(device, uuidDevice);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.dialog_uuid_not_available, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).create();
        alertDialog.show();
    }


    private String[] getBonded() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        List<String> list = new ArrayList<>();
        arrayPairedDevices = new ArrayList<>();
        for (BluetoothDevice bt : pairedDevices) {
            list.add(bt.getName());
            arrayPairedDevices.add(bt);
        }
        String[] names = new String[list.size()];
        list.toArray(names);
        return names;
    }

    private void updateStripSize() {
        // Updates the strip size
        final SharedPreferences stripSize = getSharedPreferences(MY_PREFS_FILE, Context.MODE_PRIVATE);
        mStripSize = stripSize.getInt(STRIP_SIZE, mDefaultStripSize);

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.number_picker, null);
        final NumberPicker numberPicker = view.findViewById(R.id.strip_size_picker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(256);
        numberPicker.setValue(mStripSize);
        numberPicker.setOrientation(NumberPicker.HORIZONTAL);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_strip_size_title))
                .setView(view)
                .setPositiveButton(getString(R.string.dialog_strip_size_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //OK
                        mStripSize = (byte) numberPicker.getValue();
                        SharedPreferences.Editor editor = stripSize.edit();
                        editor.putInt(STRIP_SIZE, numberPicker.getValue());
                        editor.apply();
                        Toast.makeText(MainActivity.this, R.string.strip_size_updated, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_strip_size_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Cancel
                    }
                }).create();
        alertDialog.show();
    }
}
