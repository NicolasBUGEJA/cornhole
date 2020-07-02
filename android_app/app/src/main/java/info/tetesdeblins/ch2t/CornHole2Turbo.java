package info.tetesdeblins.ch2t;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;
import java.util.UUID;

public class CornHole2Turbo extends AppCompatActivity implements View.OnClickListener {

    int REQUEST_ENABLE_BLUETOOTH = 0;
    BluetoothAdapter bluetoothAdapter = null;

    // Tag utilisé pour préfixer les logs
    public final static String TAG_LOG = "CH2T";

    // Nom du device bluetooth de la planche
    public final static String BLUETOOTH_DEVICE_NAME = "CORN HOLE 2 TURBO";

    // Classe du device bluetooth de la planche
    public final static int BLUETOOTH_DEVICE_CLASS = 7936;

    // UUID de l'application Android
    public final static UUID BLUETOOTH_ANDROID_UUID = UUID.fromString("a23f621e-bca4-11ea-b3de-0242ac130004");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setSystemUiVisibility(
//                SYSTEM_UI_FLAG_IMMERSIVE,
//                SYSTEM_UI_FLAG_FULLSCREEN,
//                SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        ImageView button = (ImageView) this.findViewById(R.id.connect_icon);
        button.setOnClickListener(this);

        // Get the default adapter and verify
        try {
            bluetoothAdapter = getBluetoothAdapter();
            verifyBluetooth();
        } catch (Exception e) {
            e.printStackTrace();
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                System.out.println(profile);
            }
            public void onServiceDisconnected(int profile) {
                System.out.println(profile);
            }
        };

    }

    /**
     * Récupération de l'adapter bluetooth
     * @return adapter bluetooth
     */
    public BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Log.w(TAG_LOG, "BLUETOOTH - NO BLUETOOTH");
            }
        }
        return bluetoothAdapter;
    }

    /**
     * Vérifie si le bluetooth est activé
     * Si non : demande à l'utiliateur de l'activer
     */
    public void verifyBluetooth() {
        if (!getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                int deviceClass = device.getBluetoothClass().getDeviceClass();
                if (BLUETOOTH_DEVICE_NAME.equalsIgnoreCase(deviceName) && BLUETOOTH_DEVICE_CLASS == deviceClass) {

                    Log.d(TAG_LOG,  String.format("BLUETOOTH - CH2T discovered : %s - %d - %s",
                            deviceName, deviceClass, deviceHardwareAddress));
                }
            }
        }
    };

    public void discoverBluetooth(View connectIcon) {
        // Vérification si bluetooth activé
        verifyBluetooth();

        // Recherche dans les device déjà appairés pour retrouver la planche
        Set<BluetoothDevice> pairedDevices = getBluetoothAdapter().getBondedDevices();

        // TODO !!!
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG_LOG,  String.format("BLUETOOTH - Appaired device discovered : %s - %s", deviceName, deviceHardwareAddress));
            }
        }

        getBluetoothAdapter().startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View view) {
        discoverBluetooth(view);
    }
}