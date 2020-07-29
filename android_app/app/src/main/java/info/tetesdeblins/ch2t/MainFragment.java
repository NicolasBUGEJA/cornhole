package info.tetesdeblins.ch2t;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Random;

import info.tetesdeblins.ch2t.common.logger.Log;


public class MainFragment extends Fragment {
    // Debugging TAG
    private static final String TAG = Constants.TAG_LOG + " MainFragment";

    private final MainActivity.IncomingHandler messageHandler;

    // Output buffer for incoming messages from the board (for debug purposes)
    private StringBuffer outStringBuffer;

    // Listener for click on START button
    private View.OnClickListener startButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startGame();
        }
    };

    public MainFragment(MainActivity.IncomingHandler messageHandler) {
        super();
        this.messageHandler = messageHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        ImageView startButton = view.findViewById(R.id.earth);
        startButton.setOnClickListener(startButtonListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }


    /**
     * Set up the UI and background operations for a new game.
     */
    private void startGame() {
        Log.d(TAG, "startGame()");
        /*Random r = new Random();
        int low = 0;
        int high = 21;
        int result[] = { r.nextInt(high-low) + low, r.nextInt(high-low) + low };*/

        Message msg = messageHandler.obtainMessage(Constants.MESSAGE_GAME_START);
        messageHandler.sendMessage(msg);
    }
}