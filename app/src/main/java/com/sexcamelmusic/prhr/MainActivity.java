package com.sexcamelmusic.prhr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.os.SystemClock;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {
    Button buttonStart;
    Button buttonSettings;
    TextView time;
    Handler handler = new Handler();
    long timeInMilliseconds = 0L;
    long initialTime;
    int secs = 0;
    int mins = 0;
    int doubleShot;
    int liquorShot;
    int finishDrink;
    int eventFrequency;
    public MediaPlayer mp;
    int gameTime;
    int events;
    int[] unavailableNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonStart = (Button) findViewById(R.id.start);
        buttonSettings = (Button) findViewById(R.id.settings);
        time = (TextView) findViewById(R.id.timer);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        buttonStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                initialTime = SystemClock.uptimeMillis();
                buttonStart.setEnabled(false);
                buttonSettings.setEnabled(false);

                SharedPreferences prefs = getSharedPreferences();
                gameTime = getGameTime(prefs);
                events = getEvents(prefs);
                calculateEvents(events, gameTime);

                handler.postDelayed(updateTimer, 0);
            }
        });

        buttonSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });
    }

    public Runnable updateTimer = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - initialTime;
            secs = (int)(timeInMilliseconds / 1000);
            mins = secs / 60;
            secs = secs % 60;

            time.setText("" + mins + ":" + String.format("%02d", secs));

            handler.postDelayed(this, 0);

            if (secs % 60 == 0) {
                time.setText("Power Hour");
                playAudio();
                flashBackground();
            } else if (secs % 60 == 1) {
                resetBackground();
            }

            if (mins == gameTime) {
                handler.removeCallbacks(updateTimer);
            }
        }
    };

    private void playAudio() {
        if (mp == null) {
            mp = MediaPlayer.create(this, R.raw.prhr);
        }
        mp.start();
    }

    private void flashBackground() {
        final View mainView = findViewById(R.id.activity_main);
        mainView.setBackgroundColor(Color.parseColor("#ffccff"));
    }

    private void resetBackground() {
        final View mainView = findViewById(R.id.activity_main);
        mainView.setBackgroundColor(Color.parseColor("#ffffff"));
    }

    private SharedPreferences getSharedPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        return prefs;
    }

    private int getGameTime(SharedPreferences prefs) {
        String time = prefs.getString("chooseTime", "60");
        int intTime = Integer.parseInt(time);

        return intTime;
    }

    private int getEvents(SharedPreferences prefs) {
        String events = prefs.getString("chooseEvents", "0");
        int intEvents = Integer.parseInt(events);

        return intEvents;
    }

    private void calculateEvents(int events, int time) {
        eventFrequency = calculateEventFrequency(events, time);

        setEvents(eventFrequency);
    }

    private void setEvents(int frequency) {
        int randomInt;

        while (frequency > 0) {
            randomInt = ThreadLocalRandom.current().nextInt(0, (gameTime + 1));

            if (useList(unavailableNumbers, randomInt)) {
                continue;
            }

            frequency--;
        }
    }

    public static boolean useList(int[] array, int value) {
        return Arrays.asList(array).contains(array, value);
    }

    private int calculateEventFrequency(int events, int time) {
        int frequency = 0;

        switch (events) {
            case 0:
                break;
            case 1:
                frequency = (time / 20);
                break;
            case 2:
                frequency = (time / 10);
                break;
            case 3:
                frequency = (time / 7);
                break;
        }

        return frequency;
    }
}
