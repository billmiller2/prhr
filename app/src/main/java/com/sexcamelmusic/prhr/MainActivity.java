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

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    TextView time;
    Handler handler = new Handler();
    long initialTime;
    int isWineHr = 0;

    final static int doubleShot = 0;
    final static int liquorShot = 1;
    final static int finishDrink = 2;

    final static  String doubleShotText = "Double Shot";
    final static  String liquorShotText = "Liquor Shot";
    final static  String finishDrinkText = "Finish Drink";

    boolean isEventTriggered = false;
    String text = null;
    int event = 0;

    int eventFrequency;
    public MediaPlayer liquorMp;
    public MediaPlayer prhrMp;
    public MediaPlayer finishMp;
    public MediaPlayer doubleMp;
    int gameTime;
    int events;
    int[] unavailableNumbers = new int[100];
    ArrayList<Integer> unavailableNums = new ArrayList<Integer>();
    int[] eventTimes = new int[100];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button buttonStart = (Button) findViewById(R.id.start);
        final Button buttonSettings = (Button) findViewById(R.id.settings);
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
                isWineHr = getWineHr(prefs);
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

    private Runnable updateTimer = new Runnable() {
        public void run() {
            long timeInMilliseconds = SystemClock.uptimeMillis() - initialTime;
            int secs = (int) (timeInMilliseconds / 1000);
            int mins = secs / 60;
            secs = secs % 60;

            time.setText("" + mins + ":" + String.format("%02d", secs));

            handler.postDelayed(this, 0);

            if ((isWineHr == 1 && secs % 60 == 0 && mins % 2 != 0) || (isWineHr == 0 && secs % 60 == 0)) {
                if (containsValue(eventTimes, mins) && mins != 0) {
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
            } else if (secs % 60 == 1) {
                resetBackground();
                isEventTriggered = false;
            }

            if (mins == gameTime) {
                handler.removeCallbacks(updateTimer);
            }
        }
    };

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

    private void playPrhr() {
        if (prhrMp == null) {
            prhrMp = MediaPlayer.create(this, R.raw.prhr);
        }
        prhrMp.start();
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

    private int getWineHr(SharedPreferences prefs) {
        String isWineHr = prefs.getString("wineHr", "0");
        int isWine = Integer.parseInt(isWineHr);

        return isWine;
    }

    private int getEvent() {
        Random random = new Random();
        return random.nextInt(3); // only 3 events for now, prob find a better way to do this
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

    private int getEvents(SharedPreferences prefs) {
        String events = prefs.getString("chooseEvent", "0");
        int intEvents = Integer.parseInt(events);

        return intEvents;
    }

    private void calculateEvents(int events, int time) {
        eventFrequency = calculateEventFrequency(events, time);

        setEvents(eventFrequency);
    }

    private void setEvents(int frequency) {
        Random random = new Random();
        int randomInt;
        int eventIndex = 0;
        int unavailableIndex = 0;

        while (frequency > 0) {
            randomInt = random.nextInt(gameTime + 1);
            if (containsValue(unavailableNumbers, randomInt)) {
                continue;
            }

            setUnavailableNumbers(randomInt, unavailableIndex);
            eventTimes[eventIndex] = randomInt;
            eventIndex++;
            unavailableIndex+= 5;
            frequency--;
        }
    }

    private void setUnavailableNumbers(int randomInt, int index) {
        int i = -2;
        for (int newIndex = index - 2; newIndex < index + 3; newIndex++) {
            if (newIndex >= 0) {
                unavailableNumbers[newIndex] = randomInt + i;
                //unavailableNums.add(randomInt + i);
            }
            i++;
        }
    }

    public static boolean containsValue(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return true;
            }
        }
        return false;
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
