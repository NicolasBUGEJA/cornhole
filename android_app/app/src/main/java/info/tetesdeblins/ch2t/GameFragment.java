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
import android.widget.ImageButton;
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

    // Listener for click on score_up1
    private View.OnClickListener scoreUp1Listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            increaseScore(0);
        }
    };

    // Listener for click on score_up2
    private View.OnClickListener scoreUp2Listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            increaseScore(1);
        }
    };

    // Listener for click on score_down1
    private View.OnClickListener scoreDown1Listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            decreaseScore(0);
        }
    };

    // Listener for click on score_down2
    private View.OnClickListener scoreDown2Listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            decreaseScore(1);
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
        drawScores(view);

        ImageButton scoreUp1Button = view.findViewById(R.id.score_up1);
        scoreUp1Button.setOnClickListener(scoreUp1Listener);

        ImageButton scoreUp2Button = view.findViewById(R.id.score_up2);
        scoreUp2Button.setOnClickListener(scoreUp2Listener);

        ImageButton scoreDown1Button = view.findViewById(R.id.score_down1);
        scoreDown1Button.setOnClickListener(scoreDown1Listener);

        ImageButton scoreDown2Button = view.findViewById(R.id.score_down2);
        scoreDown2Button.setOnClickListener(scoreDown2Listener);

    }

    private void drawScores(View view) {
        TextView score_1 = view.findViewById(R.id.score_1);
        score_1.setText(Integer.toString(this.gameService.getLeftScore()));

        TextView score_2 = view.findViewById(R.id.score_2);
        score_2.setText(Integer.toString(this.gameService.getRightScore()));
    }

    public void increaseScore(int position) {
        if(position == 0) {
            gameService.increaseLeftScore();
        } else {
            gameService.increaseRightScore();
        }
        drawScores(this.getView());
    }

    public void decreaseScore(int position) {
        if(position == 0) {
            gameService.decreaseLeftScore();
        } else {
            gameService.decreaseRightScore();
        }
        drawScores(this.getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

}