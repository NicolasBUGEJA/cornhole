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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Random;

import info.tetesdeblins.ch2t.common.logger.Log;


public class GameFragment extends Fragment {
    // Debugging TAG
    private static final String TAG = Constants.TAG_LOG + " GameFragment";

    private final MainActivity.IncomingHandler messageHandler;
    private final GameService gameService;

    // Listener for click on textview
    private View.OnClickListener textViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            testScore();
        }
    };

    public GameFragment(MainActivity.IncomingHandler messageHandler, GameService gameService) {
        super();
        this.messageHandler = messageHandler;
        this.gameService = gameService;
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
        TextView testScore = view.findViewById(R.id.textView);
        testScore.setOnClickListener(textViewListener);
    }

    public void testScore() {
        gameService.increaseLeftScore();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

}