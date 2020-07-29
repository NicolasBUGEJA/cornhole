package info.tetesdeblins.ch2t;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import info.tetesdeblins.ch2t.common.logger.Log;

public class GameService {
    // Debugging
    private static final String TAG = Constants.TAG_LOG + " GameService";

    // Member fields
    private final Handler handler;
    private int[] score = {0, 0};

    /**
     * Constructor. Prepares a new "Game" session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public GameService(Context context, Handler handler) {
        this.handler = handler;
    }

    private void increaseLeftScore() {
        Log.d(TAG, String.format("increaseLeftScore()"));
        if (score[0] < 99) {
            score[0]++;
            sendScoreMessage();
        }
    }
    private void decreaseLeftScore() {
        Log.d(TAG, String.format("decreaseLeftScore()"));
        if (score[0] > 0) {
            score[0]--;
            sendScoreMessage();
        }
    }
    private void increaseRightScore() {
        Log.d(TAG, String.format("increaseRightScore()"));
        if (score[1] < 99) {
            score[1]++;
            sendScoreMessage();
        }
    }

    private void decreaseRightScore() {
        Log.d(TAG, String.format("decreaseRightScore()"));
        if (score[1] > 0) {
            score[1]--;
            sendScoreMessage();
        }
    }

    /**
     * Send the score back to the UI Activity and Bluetooth adapter
     */
    private void sendScoreMessage() {
        Log.d(TAG, String.format("sendScoreMessage()"));
        Message msg = handler.obtainMessage(Constants.MESSAGE_SCORE_CHANGE);
        Bundle bundle = new Bundle();
        bundle.putIntArray(Constants.HANDLER_SCORE, score);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
}