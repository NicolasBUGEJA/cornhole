package info.tetesdeblins.ch2t;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Random;

import info.tetesdeblins.ch2t.common.logger.Log;


public class MainFragment extends Fragment {
    // Debugging TAG
    private static final String TAG = Constants.TAG_LOG + " MainFragment";

    // Intent among activities
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Output buffer for incoming messages from the board (for debug purposes)
    private StringBuffer outStringBuffer;

    // The bluetooth adapter
    BluetoothAdapter bluetoothAdapter = null;

    // The bluetooth service used to communicate with the arduino/board
    BluetoothService bluetoothService = null;

    // The connected board name
    String connectedBoardName = null;

    // Listener for click on BLUETOOTH button
    private View.OnClickListener bluetoothButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showDeviceListActivity();
        }
    };

    // Listener for click on START button
    private View.OnClickListener startButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startGame();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getBluetoothAdapter();
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!getBluetoothAdapter().isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (bluetoothService == null) {
            setupBluetooth();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Retrieve useful elements
        ImageView connectButton = view.findViewById(R.id.connect_icon);
        ImageView startButton = view.findViewById(R.id.earth);

        // Set the listeners
        connectButton.setOnClickListener(bluetoothButtonListener);
        startButton.setOnClickListener(startButtonListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
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

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private final Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setConnectionStatus(getString(R.string.title_connected_to, connectedBoardName));
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
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    connectedBoardName = msg.getData().getString(Constants.HANDLER_DEVICE_NAME);
                    Toast.makeText(activity, getString(R.string.connected_to_board, connectedBoardName), Toast.LENGTH_SHORT).show();
                    //
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(activity, msg.getData().getString(Constants.HANDLER_TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

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
                    setupBluetooth();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    //TODO A fixer, en l'état c'est merdique pour un éventuel mode offline
                    Log.d(TAG, "- bluetooth NOT enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving, Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
        return bluetoothAdapter;
    }

    public void showDeviceListActivity() {
        Intent serverIntent = new Intent(getActivity(), BoardListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
    }

    public void setupBluetooth() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        bluetoothService = new BluetoothService(getActivity(), messageHandler);
    }


    /**
     * Set up the UI and background operations for a new game.
     */
    private void startGame() {
        Log.d(TAG, "startGame()");
        Random r = new Random();
        int low = 0;
        int high = 21;
        int resultLeft = r.nextInt(high-low) + low;
        int resultRight = r.nextInt(high-low) + low;
        sendScore("<S" + resultLeft + "-" + resultRight + ">");

        // On initialisera les listeners si pas déjà fait
        // TODO
//        button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // Construction du score
//                String score = "18-4";
//                sendScore(message);
//            }
//        });
    }

    /**
     * Send score.
     * @param score The score to send.
     */
    private void sendScore(String score) {
        // Check that we're actually connected before trying anything
        if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (score.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send = score.getBytes();
            bluetoothService.write(send);
        }
    }

    /**
     * Updates the status on the connection status string.
     * @param resId a string resource ID
     */
    private void setConnectionStatus(int resId) {
        // TODO change text for connection status
    }

    /**
     * Updates the status on the connection status string.
     * @param subTitle status
     */
    private void setConnectionStatus(CharSequence subTitle) {
        // TODO change text for connection status
    }

}