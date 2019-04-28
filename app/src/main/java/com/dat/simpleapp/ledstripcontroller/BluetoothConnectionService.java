package com.dat.simpleapp.ledstripcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnectionService {
    // Debugging
    private static final String TAG = "BluetoothConnectionServ";

    // Name and UUID for creating server socket
    public static final String MY_NAME = "BluetoothConnectionService";
    private static final UUID MY_UUID =
            UUID.fromString("09b306e9-4c86-4ad1-b082-8eacdfd69d11");
    // This UUID was generated using a online generator
    // Can also use UUID.randomUUID()

    // Member fields
    private final BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler = new Handler();
    private int mState;

    // Interface Listener
    private OnBluetoothConnectionListener mListener;

    // Threads
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    // Constants
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor
     */
    public BluetoothConnectionService() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    /**
     * Setting up the Interface
     */
    public interface OnBluetoothConnectionListener{
        void onMessageReceived(byte[] buffer, int bytes);
        void onConnectionStateChanged(int state);
        void onDeviceConnected(BluetoothDevice device);
        void onConnectionFailed();
        void onConnectionLost();
    }

    public void setOnBluetoothConnectionListener(OnBluetoothConnectionListener listener){
        mListener = listener;
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState(){
        return mState;
    }

    /**
     * Updates the state of the connection
     */
    private synchronized void updateState(int state){
        // Updates the state
        if (mState != state){
            Log.i(TAG, "Update state to: " + state);
            mState = state;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onConnectionStateChanged(mState);
                }
            });
        }
    }

    /**
     * Start Listening for a connection
     */
    public synchronized void startListening(){
        Log.d(TAG, "startListening: start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Update state before starting the Thread
        updateState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null){
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    /**
     * Connect to a device
     */
    public synchronized void connect(BluetoothDevice device, UUID uuidDevice){
        Log.d(TAG, "connect: start");

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING){
            if (mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Update state before starting the Thread
        updateState(STATE_CONNECTING);

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, uuidDevice);
        mConnectThread.start();
    }

    /**
     * Connected to a device
     */
    private synchronized void connected(BluetoothSocket socket, final BluetoothDevice device){
        Log.d(TAG, "connected: start");

        // Cancel the thread that completed the connection
        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null){
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Update state before starting the Thread
        updateState(STATE_CONNECTED);

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onDeviceConnected(device);
            }
        });
    }

    /**
     * Stop all threads
     */
    public synchronized void stop(){
        Log.d(TAG, "stop: start");

        // Cancel the thread that completed the connection
        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread
        if (mAcceptThread != null){
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        updateState(STATE_NONE);
    }

    /**
     * Write
     */
    public void write(byte[] out){
        Log.d(TAG, "write: start");
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this){
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread{
        // The local server socket
        private final BluetoothServerSocket mBluetoothServerSocket;

        // Constructor
        public AcceptThread() {
            Log.d(TAG, "AcceptThread: Constructor: start");
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(MY_NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: listen() failed", e);
            }
            mBluetoothServerSocket = tmp;

        }

        // Run
        @Override
        public void run() {
            Log.d(TAG, "AcceptThread: run: start");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED){
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mBluetoothServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "AcceptThread: run: accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null){
                    synchronized (BluetoothConnectionService.this){
                        switch (mState){
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "run: Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "run: END AcceptThread");
        }

        public void cancel(){
            Log.d(TAG, "AcceptThread: cancel: ");
            try {
                mBluetoothServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of server failed", e);
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread{
        private final BluetoothSocket mBluetoothSocket;
        private final BluetoothDevice mBluetoothDevice;

        // Constructor
        public ConnectThread(BluetoothDevice device, UUID uuidDevice){
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mBluetoothDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(uuidDevice);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: create() failed", e);
            }
            mBluetoothSocket = tmp;
        }

        //Run
        @Override
        public void run() {
            Log.d(TAG, "ConnectThread: run: start");
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Log.d(TAG, "run: Attempting to connect");
                mBluetoothSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mBluetoothSocket.close();
                } catch (IOException e1) {
                    Log.e(TAG, "run: unable to close() socket during connection failure", e1);
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onConnectionFailed();
                    }
                });
                updateState(STATE_NONE);
                return;
            }
            Log.i(TAG, "run: The connection was successful");

            // Reset the ConnectThread because we're done
            synchronized (BluetoothConnectionService.this){
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mBluetoothSocket, mBluetoothDevice);
        }

        public void cancel(){
            Log.d(TAG, "ConnectThread: cancel: ");
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread{
        private BluetoothSocket mBluetoothSocket;
        private InputStream mInputStream;
        private OutputStream mOutputStream;

        // Constructor
        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG, "ConnectedThread: start constructor");
            mBluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: temp sockets not created", e);
            }

            mInputStream = tmpIn;
            mOutputStream = tmpOut;
        }

        @Override
        public void run() {
            Log.d(TAG, "ConnectedThread: run: start");
            byte[] mBuffer = new byte[1024];
            int mBytes;

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED){
                try {
                    // Read from the InputStream
                    mBytes = mInputStream.read(mBuffer);

                    final byte[] buffer = mBuffer;
                    final int bytes = mBytes;
                    // Send the obtained bytes
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onMessageReceived(buffer, bytes);
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "ConnectedThread: run: disconnected", e);
                    //Connection lost
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onConnectionLost();
                        }
                    });
                    updateState(STATE_NONE);
                }
            }
        }

        public void write(byte[] buffer){
            try {
                mOutputStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: write: Exception during write", e);
            }
        }

        public void cancel(){
            Log.d(TAG, "ConnectedThread: cancel: start");
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: cancel: close() of connect socket failed", e);
            }
        }
    }
}
