package com.sexcamelmusic.prhr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.util.ArrayList;
import java.util.Random;

import static com.sexcamelmusic.prhr.TimerService.serviceRunning;

public class MainActivity extends AppCompatActivity {
    TextView time;
    Handler handler = new Handler();

    String text = null;

    int secs;
    int mins;
    int events;
    int event = 0;
    int eventFrequency;
    static int gameTime;
    static int isWineHr = 0;

    boolean isEventTriggered = false;
    static boolean startUp = true;
    static boolean isLiquorShotValid = true;

    final static int doubleShot = 0;
    final static int liquorShot = 1;
    final static int finishDrink = 2;

    final static  String doubleShotText = "Double Shot";
    final static  String liquorShotText = "Liquor Shot";
    final static  String finishDrinkText = "Finish Drink";

    static ArrayList<Integer> unavailableNumbers = new ArrayList<Integer>();
    static ArrayList<Integer>eventTimes = new ArrayList<Integer>();

    public MediaPlayer liquorMp;
    public MediaPlayer prhrMp;
    public MediaPlayer finishMp;
    public MediaPlayer doubleMp;

    /**
     * Receive data from timer service
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            secs = intent.getIntExtra("secs", 0);
            mins = intent.getIntExtra("mins", 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonStartPause = (Button) findViewById(R.id.start);
        Button buttonSettings = (Button) findViewById(R.id.settings);
        time = (TextView) findViewById(R.id.timer);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        registerReceiver(receiver, new IntentFilter(TimerService.TIMER_BR));

        if (serviceRunning) {
            setButtons();
            handler.postDelayed(updateTimer, 0);
        }

        buttonStartPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startUp) {
                    startGame();
                    startUp = false;
                }

                startService(new Intent(view.getContext(), TimerService.class));
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

    /**
     * Update the main timer
     */
    private Runnable updateTimer = new Runnable() {
        public void run() {
            if (!isEventTriggered) {
                time.setText("" + mins + ":" + String.format("%02d", secs));
            }

            handler.postDelayed(this, 0);

            if ((isWineHr == 1 && secs % 60 == 0 && mins % 2 != 0) || (isWineHr == 0 && secs % 60 == 0)
                    && mins != 0
            ) {
                if (eventTimes.contains(mins)) {
                    if (!isEventTriggered) {
                        event = getEvent();
                        text = getEventText(event);
                    }
                    time.setText(text);
                    playAudio(event);

                    isEventTriggered = true;
                } else {
                    time.setText("Power Hour");
                    playPrhr();
                }

                flashBackground();
            } else if (secs % 60 == 5 || (!isEventTriggered && secs % 60 == 1)) {
                resetBackground();
                isEventTriggered = false;
            }

            if (mins >= gameTime) {
                stopService(new Intent(getApplicationContext(), TimerService.class));
                handler.removeCallbacks(updateTimer);
            }
        }
    };

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

    private void calculateEvents(int events, int time) {
        eventFrequency = calculateEventFrequency(events, time);

        setEvents(eventFrequency);
    }

    private void flashBackground() {
        final View mainView = findViewById(R.id.activity_main);
        mainView.setBackgroundColor(Color.parseColor("#ffccff"));
    }

    /**
     * Get a random special event
     * Liquor shots are capped at one per game
     * @return int
     */
    private int getEvent() {
        Random random = new Random();
        int event = random.nextInt(3); // only 3 events for now, prob find a better way to do this

        if (event == liquorShot) {
            if (!isLiquorShotValid) {
                event = getEvent();
            }
            isLiquorShotValid = false;
        }
        return event;
    }

    private int getEvents(SharedPreferences prefs) {
        String events = prefs.getString("chooseEvent", "0");
        int intEvents = Integer.parseInt(events);

        return intEvents;
    }

    private String getEventText(int event) {
        switch (event) {
            case doubleShot:
                return doubleShotText;
            case liquorShot:
                return liquorShotText;
            case finishDrink:
                return finishDrinkText;
            default:
                return null;
        }
    }

    private int getGameTime(SharedPreferences prefs) {
        String time = prefs.getString("chooseTime", "60");
        int intTime = Integer.parseInt(time);

        return intTime;
    }

    private SharedPreferences getSharedPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        return prefs;
    }

    private int getWineHr(SharedPreferences prefs) {
        String isWineHr = prefs.getString("wineHr", "0");
        int isWine = Integer.parseInt(isWineHr);

        return isWine;
    }

    private void playAudio(int event) {
        switch (event) {
            case doubleShot:
                playDoubleShot();
                break;
            case liquorShot:
                playLiquorShot();
                break;
            case finishDrink:
                playFinishDrink();
                break;
        }
    }

    private void playDoubleShot() {
        if (doubleMp == null) {
            doubleMp = MediaPlayer.create(this, R.raw.doubleshot);
        }
        doubleMp.start();
    }

    private void playFinishDrink() {
        if (finishMp == null) {
            finishMp = MediaPlayer.create(this, R.raw.finishdrink);
        }
        finishMp.start();
    }

    private void playLiquorShot() {
        if (liquorMp == null) {
            liquorMp = MediaPlayer.create(this, R.raw.liquorshot);
        }
        liquorMp.start();
    }

    private void playPrhr() {
        if (prhrMp == null) {
            prhrMp = MediaPlayer.create(this, R.raw.prhr);
        }
        prhrMp.start();
    }

    /**
     * Disable settings button and change start
     * button to pause when game starts
     */
    private void setButtons() {
        Button buttonStartPause = (Button) findViewById(R.id.start);
        Button buttonSettings = (Button) findViewById(R.id.settings);

        buttonStartPause.setText("Pause"); // fake pause button
        buttonSettings.setEnabled(false); // get user game settings
    }

    /**
     * Set the times that events will occur
     * @param frequency  how many events will occur
     */
    private void setEvents(int frequency) {
        Random random = new Random();
        int randomInt;

        while (frequency > 0) {
            randomInt = random.nextInt(gameTime + 1);
            if (unavailableNumbers.contains(randomInt)) {
                continue;
            }

            setUnavailableNumbers(randomInt);
            eventTimes.add(randomInt);
            frequency--;
        }
    }

    /**
     * Set unavailable numbers for events
     * @param eventTime  the randomly generated event time
     */
    private void setUnavailableNumbers(int eventTime) {
        // set a buffer of three numbers before and after the event
        for (int j = 0; j < 5; j++) {
            unavailableNumbers.add(eventTime - 3 + j);
        }
    }

    private void startGame() {
        setButtons(); // disable settings and set pause button
        SharedPreferences prefs = getSharedPreferences();
        gameTime = getGameTime(prefs);
        events = getEvents(prefs);
        isWineHr = getWineHr(prefs);

        calculateEvents(events, gameTime); //set random event times
        handler.postDelayed(updateTimer, 0); // start runnable
    }

    /**
     * Reset the background flash
     */
    private void resetBackground() {
        final View mainView = findViewById(R.id.activity_main);
        mainView.setBackgroundColor(Color.parseColor("#ffffff"));
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, TimerService.class));
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        onSaveInstanceState(new Bundle());
        super.onPause();

        unregisterReceiver(receiver);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        registerReceiver(receiver, new IntentFilter(TimerService.TIMER_BR));
        handler.postDelayed(updateTimer, 0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("gameTime", gameTime);
        outState.putInt("mins", mins);
        outState.putInt("secs", secs);
        outState.putBoolean("startUp", startUp);

        handler.removeCallbacks(updateTimer);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        setButtons(); // disable settings and set pause button

        time = (TextView) findViewById(R.id.timer);
        gameTime = savedInstanceState.getInt("gameTime");
        mins = savedInstanceState.getInt("mins");
        secs = savedInstanceState.getInt("secs");
        startUp = savedInstanceState.getBoolean("startUp");
        handler.postDelayed(updateTimer, 0);
    }
}
