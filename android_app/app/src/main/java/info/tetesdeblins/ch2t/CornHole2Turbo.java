package info.tetesdeblins.ch2t;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

public class CornHole2Turbo extends AppCompatActivity {
    // Debugging
    private static final String TAG = Constants.TAG_LOG + " MainActivity";

    // Codes pour les Intents (entre activités)
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Buffer pour les messages en provenance de la planche (pour du debug éventuel)
    private StringBuffer outStringBuffer;

    BluetoothAdapter bluetoothAdapter = null;
    BluetoothService bluetoothService = null;
    String connectedDeviceName = null;

    // Listener sur le click sur le bouton BLUETOOTH
    private View.OnClickListener bluetoothIconListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showDeviceListActivity();
        }
    };

    // Listener sur le click sur le bouton START
    private View.OnClickListener startGameListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startGame();
        }
    };

    // A la création de l'appli
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // On cache kes menus android
        hideSystemUI();

        // Ajout du listener sur le bouton de connection bluetooth
        // TODO utiliser les fragments..
        this.findViewById(R.id.connect_icon).setOnClickListener(bluetoothIconListener);
        // TODO NICO !!! this.findViewById(R.id.start_button).setOnClickListener(startGameListener);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
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
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }


    private CornHole2Turbo getActivity() {
        return this;
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
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            //setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            //setStatus(R.string.title_not_connected);
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
                    connectedDeviceName = msg.getData().getString(Constants.HANDLER_DEVICE_NAME);
                    Toast.makeText(activity, "Connected to " + connectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(activity, msg.getData().getString(Constants.HANDLER_TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupBluetooth();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Etablissement de la connexion avec la planche
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
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
        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Log.w(TAG, "BLUETOOTH - NO BLUETOOTH");
                Toast.makeText(this, "Le Bluetooth n'est pas disponible sur votre téléphone", Toast.LENGTH_LONG).show();
               // this.finish();
            }
        }
        return bluetoothAdapter;
    }

    public void showDeviceListActivity() {
        Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
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