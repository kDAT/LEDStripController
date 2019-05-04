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
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

    Mode staticMode;
    Mode dynamicMode;
    Mode timerMode;
    Mode alarmMode;
    Mode climateMode;

    private int mDefaultStripSize = 30;
    private int mStripSize;
    public static final String MY_PREFS_FILE = "My_Preferences_File";
    public static final String STRIP_SIZE = "Strip_Size";
    public static final String BUNDLE_STATIC = "Bundle_Static";
    public static final String BUNDLE_DYNAMIC = "Bundle_Dynamic";
    public static final String BUNDLE_ALARM = "Bundle_Alarm";
    public static final String BUNDLE_TIMER = "Bundle_Timer";
    public static final String BUNDLE_CLIMATE = "Bundle_Climate";
    public static final String FRAG_MANUAL = "Manual_Fragment";
    public static final String FRAG_ALARM = "Alarm_Fragment";
    public static final String FRAG_TIMER = "Timer_Fragment";
    public static final String FRAG_CLIMATE = "Climate_Fragment";

    private DrawerLayout mDrawerLayout;
    private ActionBar actionBar;

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

        // Creates the objects of each mode
        setupModes(savedInstanceState);

        // Sets up the navigationDrawer
        setupNavigationDrawer(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
                        mStripSize = numberPicker.getValue();
                        SharedPreferences.Editor editor = stripSize.edit();
                        editor.putInt(STRIP_SIZE, mStripSize);
                        editor.apply();
                        updateFragments();
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

    public void updateFragments(){
        // TODO update the strip size to the fragments
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByteArray(BUNDLE_STATIC, staticMode.getBytes());
        outState.putByteArray(BUNDLE_DYNAMIC, dynamicMode.getBytes());
        outState.putByteArray(BUNDLE_ALARM, alarmMode.getBytes());
        outState.putByteArray(BUNDLE_TIMER, timerMode.getBytes());
        outState.putByteArray(BUNDLE_CLIMATE, climateMode.getBytes());
    }

    private void setupModes(Bundle savedInstanceState) {
        //Define the default values for each mode
        byte[] bytesStatic = new byte[16];
        byte[] bytesDynamic = new byte[16];
        byte[] bytesAlarm = new byte[16];
        byte[] bytesTimer = new byte[16];
        byte[] bytesClimate = new byte[16];

        if (savedInstanceState != null) {
            bytesStatic = savedInstanceState.getByteArray(BUNDLE_STATIC);
            bytesDynamic = savedInstanceState.getByteArray(BUNDLE_DYNAMIC);
            bytesAlarm = savedInstanceState.getByteArray(BUNDLE_ALARM);
            bytesTimer = savedInstanceState.getByteArray(BUNDLE_TIMER);
            bytesClimate = savedInstanceState.getByteArray(BUNDLE_CLIMATE);
        } else {
            //Static mode
            bytesStatic[0] = (byte) 1;             // Mode static
            bytesStatic[1] = (byte) 255;           // Bright 0-255
            bytesStatic[2] = (byte) 1;             // Start Position 1-stripSize
            bytesStatic[3] = (byte) mStripSize;    // End Position 1-stripSize
            bytesStatic[4] = (byte) 0;             // Red 0-255
            bytesStatic[5] = (byte) 0;             // Green 0-255
            bytesStatic[6] = (byte) 0;             // Blue 0-255
            bytesStatic[15] = (byte) mStripSize;   // Strip Size

            // Dynamic Mode
            bytesDynamic[0] = (byte) 2;             // Mode dynamic
            bytesDynamic[1] = (byte) 255;           // Bright 0-255
            bytesDynamic[2] = (byte) 176;           // Speed 0-255
            bytesDynamic[3] = (byte) 0;             // Wave type 0-6
            bytesDynamic[4] = (byte) 255;           // Width 0-255
            bytesDynamic[5] = (byte) 0;             // Inverted 0-1 (boolean)
            bytesDynamic[6] = (byte) 0;             // Red 0-255
            bytesDynamic[7] = (byte) 0;             // Green 0-255
            bytesDynamic[8] = (byte) 0;             // Blue 0-255
            bytesDynamic[15] = (byte) mStripSize;   // Strip Size

            // Alarm Mode
            bytesAlarm[0] = (byte) 4;             // Mode alarm
            bytesAlarm[1] = (byte) 255;           // Bright 0-255
            bytesAlarm[2] = (byte) 0;             // Current hour 0-23
            bytesAlarm[3] = (byte) 0;             // Current minute 0-59
            bytesAlarm[4] = (byte) 0;             // Current second 0-59
            bytesAlarm[5] = (byte) 0;             // Alarm hour 0-23
            bytesAlarm[6] = (byte) 0;             // Alarm minute 0-59
            bytesAlarm[7] = (byte) 0;             // Lighting time
            bytesAlarm[8] = (byte) 255;           // Red 0-255
            bytesAlarm[9] = (byte) 255;           // Green 0-255
            bytesAlarm[10] = (byte) 255;          // Blue 0-255
            bytesAlarm[15] = (byte) mStripSize;   // Strip Size

            // Timer Mode
            bytesTimer[0] = (byte) 5;             // Mode alarm
            bytesTimer[1] = (byte) 255;           // Bright 0-255
            bytesTimer[2] = (byte) 0;             // Hours 0-99
            bytesTimer[3] = (byte) 1;             // Minutes 0-59
            bytesTimer[4] = (byte) 0;             // Seconds 0-59
            bytesTimer[5] = (byte) 0;             // Pause 0-1
            bytesTimer[15] = (byte) mStripSize;   // Strip Size

            // Climate Mode
            bytesClimate[0] = (byte) 3;             // Mode alarm
            bytesClimate[1] = (byte) 0;             // Climate type 0-8
            bytesClimate[15] = (byte) mStripSize;   // Strip Size
        }

        staticMode = new Mode(bytesStatic, mBluetoothConnectionService);
        dynamicMode = new Mode(bytesDynamic, mBluetoothConnectionService);
        alarmMode = new Mode(bytesAlarm, mBluetoothConnectionService);
        timerMode = new Mode(bytesTimer, mBluetoothConnectionService);
        climateMode = new Mode(bytesClimate, mBluetoothConnectionService);
    }

    /**
     * Navigation Drawer stuff
     */
    private void setupNavigationDrawer(Bundle savedInstanceState) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return navigationItemSelected(menuItem);
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //To open the message fragment at the start of the app
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, ManualFragment.newInstance(), FRAG_MANUAL).commit();
            actionBar.setTitle(R.string.nav_manual);
            navigationView.setCheckedItem(R.id.nav_manual);
        }
    }

    private boolean navigationItemSelected(MenuItem menuItem) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (menuItem.getItemId()) {
            case R.id.nav_manual:
                fragmentTransaction.replace(R.id.fragment_container, ManualFragment.newInstance(), FRAG_MANUAL).commit();
                actionBar.setTitle(R.string.nav_manual);
                break;
            case R.id.nav_alarm:
                fragmentTransaction.replace(R.id.fragment_container, AlarmFragment.newInstance(), FRAG_ALARM).commit();
                actionBar.setTitle(R.string.nav_alarm);
                break;
            case R.id.nav_timer:
                fragmentTransaction.replace(R.id.fragment_container, TimerFragment.newInstance(), FRAG_TIMER).commit();
                actionBar.setTitle(R.string.nav_timer);
                break;
            case R.id.nav_climate:
                fragmentTransaction.replace(R.id.fragment_container, ClimateFragment.newInstance(), FRAG_CLIMATE).commit();
                actionBar.setTitle(R.string.nav_climate);
                break;
            default:
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
