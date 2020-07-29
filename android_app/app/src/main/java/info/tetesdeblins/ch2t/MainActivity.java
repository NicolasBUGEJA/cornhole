/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.tetesdeblins.ch2t;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import java.lang.ref.WeakReference;

import info.tetesdeblins.ch2t.common.logger.Log;
import info.tetesdeblins.ch2t.common.logger.LogFragment;
import info.tetesdeblins.ch2t.common.logger.LogWrapper;
import info.tetesdeblins.ch2t.common.logger.MessageOnlyLogFilter;

/**
 * Base launcher activity, to handle most of the common plumbing
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = Constants.TAG_LOG + " MainActivity";

    // Manifest autorization
    private static final int MANIFEST_ACCESS_FINE_LOCATION = 200;

    // Intent among activities
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;

    // The connected board name
    private static String connectedBoardName = null;

    // The bluetooth adapter
    BluetoothAdapter bluetoothAdapter = null;

    // The bluetooth service used to communicate with the arduino/board
    BluetoothService bluetoothService = null;

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private IncomingHandler messageHandler;

    // Listener for click on BLUETOOTH button
    private View.OnClickListener bluetoothButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showDeviceListActivity();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeHandler(this);
        setContentView(R.layout.activity_main);

        // Hiding android menus
        hideSystemUI();

        // Ask for bluetooth permissions
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MANIFEST_ACCESS_FINE_LOCATION);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            MainFragment fragment = new MainFragment(messageHandler);
            transaction.replace(R.id.main_content_fragment, fragment);
            transaction.commit();
        }

        // Retrieve useful elements
        ImageView connectButton = findViewById(R.id.connect_icon);

        // Set the listeners
        connectButton.setOnClickListener(bluetoothButtonListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(true);
        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                View logFragmentView = findViewById(R.id.log_fragment_layout);
                logFragmentView.setVisibility(mLogShown ? View.VISIBLE : View.GONE);
                invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeLogging();

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!getBluetoothAdapter().isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (bluetoothService == null) {
            initializeBluetooth(messageHandler);
            messageHandler.setBluetoothService(bluetoothService);
        }
    }

    /**
     * Create a chain of targets that will receive log data
     **/
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Logging initialized");
    }

    /**
     * Create a chain of targets that will receive log data
     * @param activity The main activity
     *
     **/
    public void initializeHandler(FragmentActivity activity) {
        messageHandler = new IncomingHandler(activity);
    }

    /**
     * Create the bluetooth service
     */
    public void initializeBluetooth(Handler handler) {
        // Initialize the BluetoothChatService to perform bluetooth connections
        bluetoothService = new BluetoothService(this, handler);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (bluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (bluetoothService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                bluetoothService.start();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                Log.d(TAG, "onActivityResult() - REQUEST_CONNECT_DEVICE_SECURE");
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                Log.d(TAG, "onActivityResult() - REQUEST_CONNECT_DEVICE_INSECURE");
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                Log.d(TAG, "onActivityResult() - REQUEST_ENABLE_BT");
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "- bluetooth enabled");
                    // Bluetooth is now enabled, so set up a chat session
                    initializeBluetooth(messageHandler);
                } else {
                    // User did not enable Bluetooth or an error occurred
                    //TODO A fixer, en l'état c'est merdique pour un éventuel mode offline
                    Log.d(TAG, "- bluetooth NOT enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    this.finish();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Handler to handle every messages sent between fragments and views
     */
    static class IncomingHandler extends Handler {
        public FragmentActivity activity;

        private WeakReference<BluetoothService> bluetoothService;

        public IncomingHandler(FragmentActivity activity) {
            this.activity = activity;
        }

        public void setBluetoothService(BluetoothService service) {
            this.bluetoothService = new WeakReference<>(service);;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_BLUETOOTH_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setConnectionStatus(activity.getApplicationContext().getString(R.string.title_connected_to, connectedBoardName));
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setConnectionStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setConnectionStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_BLUETOOTH_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_BLUETOOTH_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    connectedBoardName = msg.getData().getString(Constants.HANDLER_DEVICE_NAME);
                    Toast.makeText(activity, activity.getApplicationContext().getString(R.string.connected_to_board, connectedBoardName), Toast.LENGTH_SHORT).show();
                    //
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(activity, msg.getData().getString(Constants.HANDLER_TOAST), Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_SCORE_CHANGE:
                    int[] score = msg.getData().getIntArray(Constants.HANDLER_SCORE);
                    sendScore(activity, bluetoothService.get(), score);
            }
        }
    }

    /**
     * Récupération de l'adapter bluetooth
     * @return adapter bluetooth
     */
    public BluetoothAdapter getBluetoothAdapter() {
        Log.d(TAG, "getBluetoothAdapter()");
        if (bluetoothAdapter == null) {
            Log.d(TAG, "- getDefaultAdapter called");
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Log.w(TAG, "- NO BLUETOOTH ADAPTER, finishing activity");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
        return bluetoothAdapter;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MANIFEST_ACCESS_FINE_LOCATION: {
                Log.d(TAG, "onRequestPermissionsResult() - ACCESS_FINE_LOCATION");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "- Permission granted for ACCESS_FINE_LOCATION");
                    }
                    else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "- Permission denied for ACCESS_FINE_LOCATION");
                        Toast.makeText(this, R.string.bt_not_authorized, Toast.LENGTH_SHORT).show();
                    }
                }
                return;
            }
        }
    }

    public void showDeviceListActivity() {
        Intent serverIntent = new Intent(this, BoardListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
    }

    /**
     * Connection to the board
     *
     * @param data   An {@link Intent} with {@link BoardListActivity#EXTRA_BOARD_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        Log.d(TAG, "connectDevice()");
        // Get the device MAC address
        String address = data.getExtras().getString(BoardListActivity.EXTRA_BOARD_ADDRESS);
        Log.d(TAG, String.format("- secure : %b , board address : %s", secure, address));

        // Get the BluetoothDevice object
        BluetoothDevice device = getBluetoothAdapter().getRemoteDevice(address);

        // Attempt to connect to the device
        bluetoothService.connect(device, secure);
    }

    /**
     * Updates the status on the connection status string.
     * @param resId a string resource ID
     */
    private static void setConnectionStatus(int resId) {
        // TODO change text for connection status
    }

    /**
     * Updates the status on the connection status string.
     * @param subTitle status
     */
    private static void setConnectionStatus(CharSequence subTitle) {
        // TODO change text for connection status
    }

    /**
     * Send score.
     * @param activity
     * @param bluetoothService
     * @param score The score to send.
     */
    private static void sendScore(FragmentActivity activity, BluetoothService bluetoothService, int[] score) {
        // Check that we're actually connected before trying anything
        if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(activity, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (score[0] > 0 && score[1] > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send = String.format("<S%d-%d>", score[0], score[1]).getBytes();
            bluetoothService.write(send);
        }
    }

    /**
     * Hide system UI
     */
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
