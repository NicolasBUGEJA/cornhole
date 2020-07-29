package info.tetesdeblins.ch2t;

import java.util.UUID;

/**
 * Defines several constants used between {@link BluetoothService} and the UI.
 */
public interface Constants {

    // Message types sent from the services Handlers
    public static final int MESSAGE_BLUETOOTH_STATE_CHANGE = 1;
    public static final int MESSAGE_BLUETOOTH_READ = 2;
    public static final int MESSAGE_BLUETOOTH_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_SCORE_CHANGE = 6;

    // Handler variables
    public static final String HANDLER_DEVICE_NAME = "device_name";
    public static final String HANDLER_SCORE = "score";
    public static final String HANDLER_TOAST = "toast";

    // TAG for log prefix
    public static final String TAG_LOG = "CH2T";

    // Generic name for CH2T arduino bluetooth device
    // TODO Rendre la récupération de la planche dynamique
    public static final String BLUETOOTH_DEVICE_NAME = "CORN HOLE 2 TURBO";

    // CH2T arduino bluetooth device class
    // TODO trouver d'autres identifiants uniques du type de capteur, un truc propre à CH2T
    public static final int BLUETOOTH_DEVICE_CLASS = 7936;

    // UUID for secure bluetooth connexion
    // Note : Not used by now since the bluetooth module used is kinda basic
    public static final UUID BLUETOOTH_ANDROID_UUID_SECURE = UUID.fromString("a23f621e-bca4-11ea-b3de-0242ac130004");

    // UUID for insecure bluetooth connexion
    // Note : The generic UUID used here is mandatory for serial bluetooth (like the one used in CH2T..)
    public static final UUID BLUETOOTH_ANDROID_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
}