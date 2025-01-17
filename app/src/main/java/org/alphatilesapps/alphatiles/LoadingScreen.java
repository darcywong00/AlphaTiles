package org.alphatilesapps.alphatiles;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import static org.alphatilesapps.alphatiles.Start.hasSyllableAudio;
import static org.alphatilesapps.alphatiles.Start.wordAudioIDs;
import static org.alphatilesapps.alphatiles.Start.wordDurations;
import static org.alphatilesapps.alphatiles.Start.wordList;
import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.totalAudio;
import static org.alphatilesapps.alphatiles.Start.tileAudioIDs;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.tileDurations;
import static org.alphatilesapps.alphatiles.Start.hasTileAudio;
import static org.alphatilesapps.alphatiles.Start.syllableAudioIDs;
import static org.alphatilesapps.alphatiles.Start.syllableList;
import static org.alphatilesapps.alphatiles.Start.syllableDurations;
import static org.alphatilesapps.alphatiles.Start.correctSoundID;
import static org.alphatilesapps.alphatiles.Start.incorrectSoundID;
import static org.alphatilesapps.alphatiles.Start.correctFinalSoundID;
import static org.alphatilesapps.alphatiles.Start.correctSoundDuration;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class LoadingScreen extends AppCompatActivity {

    //JP June 2022: moved loading of all SoundPool audio into this activity
    //note: audio instructions use MediaPlayer, not SoundPool

    private Handler mHandler = new Handler();
    Context context;
    ProgressBar progressBar;
    int maxWordWidthInPixels = 39;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        progressBar = findViewById(R.id.progressBar);
        context = this;

        String verName = BuildConfig.VERSION_NAME;
        TextView versionNumberView = (TextView) findViewById(R.id.versionNumber);
        versionNumberView.setText(getString(R.string.ver_info, verName));


        int num_of_words = wordList.size();

        Intent intent = new Intent(this, ChoosePlayer.class);

        // load audio in background threads to avoid blocking UI thread

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadGameAudio();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadWordAudio();
            }
        }).start();

        if (hasTileAudio) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadTileAudio();
                }
            }).start();
        }

        if (hasSyllableAudio) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadSyllableAudio();
                }
            }).start();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadPixelWidthAdjustments();
            }
        }).start();

        //JP: alpha tiles colors separated into r,g,b
        //ex: the first color 6200EE corresponds to 98, 0, 1 in the 0 index of each array
        //for the progress bar
        int[] reds = {98, 55, 3, 0, 156, 33, 244, 76, 233};
        int[] greens = {0, 0, 218, 255, 39, 150, 67, 175, 30};
        int[] blues = {238, 179, 197, 0, 176, 243, 54, 80, 99};

        final int[] color_index = {0};
        final int[] mod_color = {0};

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                color_index[0]++; //JP: used w/mod_color to iterate through colors in reds, greens,
                // blues arrays
                mod_color[0] = color_index[0] % 9; //JP: 9 alpha tiles colors in use
                // (removed yellow and white), so indices 0-8
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.getProgressDrawable().setColorFilter(
                                Color.rgb(reds[mod_color[0]], greens[mod_color[0]], blues[mod_color[0]]),
                                android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                });
            }
        }, 0, 1500);//wait 0 ms before doing the action and do it every 1500ms (1.5 sec)

        final int[] audio_loaded = {0}; //JP: tracks how many audio files have already been loaded
        gameSounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

            float percentage = 0.0F;

            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                //JP: this function is called when ANY audio file in the gameSounds SoundPool
                //has finished loading, regardless of what activity that sound was loaded in

                audio_loaded[0]++; //JP: tracks how many audio files have been loaded so far

                percentage = ((float) audio_loaded[0] / (float) totalAudio) * 100;

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress((int) percentage);
                    }
                });

                //once all audio files have finished loading, launch ChoosePlayer
                if (audio_loaded[0] == totalAudio) {
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public void loadWordAudio() {
        // load speech sounds
        Resources res = context.getResources();
        wordAudioIDs = new HashMap();
        wordDurations = new HashMap();

        for (Start.Word word : wordList) {
            int resId = res.getIdentifier(word.nationalWord, "raw", context.getPackageName());
            int duration = getAssetDuration(resId) + 100;
            wordAudioIDs.put(word.nationalWord, gameSounds.load(context, resId, 1));
            wordDurations.put(word.nationalWord, duration);
            word.duration = duration;
        }
    }

    public void loadSyllableAudio() {
        Resources res = context.getResources();
        syllableAudioIDs = new HashMap();
        syllableDurations = new HashMap();

        for (Start.Syllable syll : syllableList) {
            int resId = res.getIdentifier(syll.syllableAudioName, "raw", context.getPackageName());
            int duration = getAssetDuration(resId) + 100;
            syllableAudioIDs.put(syll.syllable, gameSounds.load(context, resId, 2));
            syllableDurations.put(syll.syllable, duration);
            syll.syllableDuration = duration;
        }
    }

    public void loadTileAudio() {
        Resources res = context.getResources();
        tileAudioIDs = new HashMap(0);
        tileDurations = new HashMap();

        for (Start.Tile tile : tileList) {
            int resId = res.getIdentifier(tile.audioForTile, "raw", context.getPackageName());
            int duration = getAssetDuration(resId) + 100;
            tileAudioIDs.put(tile.baseTile, gameSounds.load(context, resId, 2));
            tileDurations.put(tile.baseTile, duration);
            tile.tileDuration1 = duration;

            if (!tile.tileTypeB.equals("none")) {
                if (!tile.audioForTileB.equals("X")) {
                    resId = res.getIdentifier(tile.audioForTileB, "raw", context.getPackageName());
                    duration = getAssetDuration(resId) + 100;
                    tileAudioIDs.put(tile.baseTile + "B", gameSounds.load(context, resId, 2));
                    tileDurations.put(tile.baseTile + "B", duration);
                    tile.tileDuration2 = duration;
                    totalAudio++;
                }
            }
            if (tile.tileTypeC.compareTo("none") != 0) {
                if (tile.audioForTileC.compareTo("X") != 0) {
                    resId = res.getIdentifier(tile.audioForTileC, "raw", context.getPackageName());
                    duration = getAssetDuration(resId) + 100;
                    tileAudioIDs.put(tile.baseTile + "C", gameSounds.load(context, resId, 2));
                    tileDurations.put(tile.baseTile + "C", duration);
                    tile.tileDuration3 = duration;
                    totalAudio++;
                }
            }

        }
    }

    public void loadGameAudio() {
        // load music sounds
        correctSoundID = gameSounds.load(context, R.raw.zz_correct, 3);
        incorrectSoundID = gameSounds.load(context, R.raw.zz_incorrect, 3);
        correctFinalSoundID = gameSounds.load(context, R.raw.zz_correct_final, 1);

        correctSoundDuration = getAssetDuration(R.raw.zz_correct) + 200;
        //		incorrectSoundDuration = getAssetDuration(R.raw.zz_incorrect);	// not needed atm
        //		correctFinalSoundDuration = getAssetDuration(R.raw.zz_correct_final);	// not needed atm
    }


    private int getAssetDuration(int assetID) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        AssetFileDescriptor afd = context.getResources().openRawResourceFd(assetID);
        mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        return Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    public void loadPixelWidthAdjustments() {

        for (Start.Word word : wordList) {
            word.adjustment = String.valueOf(calculatedPixelWidthAdjustment(word.localWord));
        }
    }

    private double calculatedPixelWidthAdjustment(String word) {
        TextView wordView = new TextView(this);
        wordView.setText(word);
        wordView.setTextSize(11);
        wordView.measure(0, 0);
        int wordWidthInPixels = wordView.getMeasuredWidth();

        if (wordWidthInPixels <= maxWordWidthInPixels) {
            return 1;
        } else {
            return Math.round((maxWordWidthInPixels * 100.0) / (wordWidthInPixels * 100));
        }

    }


}


