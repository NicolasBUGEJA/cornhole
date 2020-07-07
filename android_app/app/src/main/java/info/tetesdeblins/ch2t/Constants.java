package info.tetesdeblins.ch2t;

import java.util.UUID;

/**
 * Defines several constants used between {@link BluetoothService} and the UI.
 */
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Handler variables
    public static final String HANDLER_DEVICE_NAME = "device_name";
    public static final String HANDLER_TOAST = "toast";

    // Tag utilisé pour préfixer les logs
    public static final String TAG_LOG = "CH2T";

    // Nom du device bluetooth de la planche
    // TODO Rendre la récupération de la planche dynamique
    public static final String BLUETOOTH_DEVICE_NAME = "CORN HOLE 2 TURBO";

    // Classe du device bluetooth de la planche
    public static final int BLUETOOTH_DEVICE_CLASS = 7936;

    // UUID de l'application Android
    public static final UUID BLUETOOTH_ANDROID_UUID_SECURE = UUID.fromString("a23f621e-bca4-11ea-b3de-0242ac130004");

    // UUID de l'application Android
    public static final UUID BLUETOOTH_ANDROID_UUID_INSECURE = UUID.fromString("4cf03a56-bfc7-11ea-b3de-0242ac130004");
}