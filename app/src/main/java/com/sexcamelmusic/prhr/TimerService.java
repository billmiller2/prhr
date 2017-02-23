package com.sexcamelmusic.prhr;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import static com.sexcamelmusic.prhr.R.id.text;

/**
 * Timer Service
 *
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class TimerService extends Service {
    Handler handler = new Handler();

    public static final String TIMER_BR = "sexcamelmusic.timer_br";
    Intent timerIntent = new Intent(TIMER_BR);

    long initialTime;
    int secs;
    int addedTime;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        initialTime = SystemClock.uptimeMillis();
        handler.postDelayed(getTime, 0);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        addedTime += intent.getIntExtra("addedTime", 0);

        return START_STICKY;
    }

    private Runnable getTime = new Runnable() {
        public void run() {
            long timeInMilliseconds = SystemClock.uptimeMillis() - initialTime;
            secs = (int) (timeInMilliseconds / 1000) + addedTime;
            int mins = secs / 60;
            secs = secs % 60;

            timerIntent.putExtra("secs", secs);
            timerIntent.putExtra("mins", mins);
            sendBroadcast(timerIntent);

            handler.postDelayed(this, 0);
        }
    };
}
