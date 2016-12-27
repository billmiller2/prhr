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

public class MainActivity extends AppCompatActivity {
    Button buttonStart;
    Button buttonSettings;
    TextView time;
    Handler handler = new Handler();
    Handler flash = new Handler();
    long timeInMilliseconds = 0L;
    long initialTime;
    int secs = 0;
    int mins = 0;
    public MediaPlayer mp;
    String gameTime;

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
                gameTime = checkPreferenceValues();

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
            }

            if (mins == Integer.parseInt(gameTime)) {
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

        flash.postDelayed(new Runnable() {
            @Override
            public void run() {
                mainView.setBackgroundColor(Color.parseColor("#ffffff"));
            }
        }, 500);
    }

    private String checkPreferenceValues()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        String time = prefs.getString("chooseTime", "60");

        return time;
    }
}
