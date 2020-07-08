/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.tetesdeblins.ch2t;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;
import java.util.stream.Collectors;

import info.tetesdeblins.ch2t.common.logger.Log;

/**
 * This Activity appears as a dialog. It lists any paired boards and
 * boards detected in the area after discovery. When a board is chosen
 * by the user, the MAC address of the arduino bluetooth is sent back to the parent
 * Activity in the result Intent.
 */
public class BoardListActivity extends Activity {

    /**
     * Tag for Log
     */
    private static final String TAG = Constants.TAG_LOG + " BoardListActivity";

    /**
     * Return Intent extra
     */
    public static String EXTRA_BOARD_ADDRESS = "device_address";

    /**
     * Member fields
     */
    private BluetoothAdapter bluetoothAdapter;

    /**
     * Newly discovered boards
     */
    private ArrayAdapter<String> boardsArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        setContentView(R.layout.activity_board_list);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        Button scanButton = findViewById(R.id.button_scan);
        scanButton.setOnClickListener(view -> {
            doDiscovery();
            view.setVisibility(View.GONE);
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        ArrayAdapter<String> pairedBoardsArrayAdapter =
                new ArrayAdapter<>(this, R.layout.device_name);
        boardsArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedBoardsArrayAdapter);
        pairedListView.setOnItemClickListener(itemClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(boardsArrayAdapter);
        newDevicesListView.setOnItemClickListener(itemClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(broadcastReceiver, filter);

        // Get the local Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired boards
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices().stream()
                .filter(device -> Constants.BLUETOOTH_DEVICE_NAME.startsWith(device.getName()) &&
                        Constants.BLUETOOTH_DEVICE_CLASS == device.getBluetoothClass().getDeviceClass())
                .collect(Collectors.toSet());

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedBoardsArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedBoardsArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        // Make sure we're not doing discovery anymore
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "- cancelDiscovery called");
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(broadcastReceiver);
        Log.d(TAG, "- broadcastReceiver unregistered");
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "- cancelDiscovery called");
        }

        // Request discover from BluetoothAdapter
        bluetoothAdapter.startDiscovery();
        Log.d(TAG, "- startDiscovery called");
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener itemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "onItemClick()");
            // Cancel discovery because it's costly and we're about to connect
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "- cancelDiscovery called");

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_BOARD_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            Log.d(TAG, "- finishing BoardListActivity");
            finish();
        }
    };

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String deviceName = device.getName();
                    int deviceClass = device.getBluetoothClass().getDeviceClass();
                    String deviceAddress = device.getAddress();

                    // If the device is a CH2T board, add it to the arrayAdapter
                    if (deviceName != null &&
                            deviceName.startsWith(Constants.BLUETOOTH_DEVICE_NAME) &&
                            Constants.BLUETOOTH_DEVICE_CLASS == deviceClass) {
                        boardsArrayAdapter.add(deviceName + "\n" + deviceAddress);
                        Log.d(TAG, String.format("CH2T board discovered : %s - %d - %s",
                                deviceName, deviceClass, deviceAddress));
                    }
                    else {
                        Log.d(TAG, String.format("device discovered : %s - %d - %s",
                                deviceName, deviceClass, deviceAddress));
                    }
                }
            }

            // When discovery mode is ended
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setTitle(R.string.select_device);
                if (boardsArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    boardsArrayAdapter.add(noDevices);
                }
            }
        }
    };

}
