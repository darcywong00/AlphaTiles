package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.graphics.Typeface;
import android.widget.Button;

import static org.alphatilesapps.alphatiles.Start.*;

public class UnitedStates extends GameActivity {

    int upperTileLimit = 5;
    int neutralFontSize;
    String[] selections = new String[]{"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""}; // KP
    String lastWord = "";
    String secondToLastWord = "";
    String thirdToLastWord = "";
    int unitedStatesPoints;
    boolean unitedStatesHasChecked12Trackers;

    protected static final int[] TILE_BUTTONS = {
            R.id.button01a, R.id.button01b, R.id.button02a, R.id.button02b, R.id.button03a, R.id.button03b, R.id.button04a, R.id.button04b, R.id.button05a, R.id.button05b,
            R.id.button06a, R.id.button06b, R.id.button07a, R.id.button07b, R.id.button08a, R.id.button08b, R.id.button09a, R.id.button09b
    };

    protected int[] getTileButtons() {
        return TILE_BUTTONS;
    }

    protected int[] getWordImages() {
        return null;
    }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try {
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());
        } catch (NullPointerException e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void centerGamesHomeImage() {
        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = 0;
        switch (challengeLevel) {
            case 1:
                gameID = R.id.united_states_cl1_CL;
                break;
            case 2:
                gameID = R.id.united_states_cl2_CL;
                break;
            case 3:
                gameID = R.id.united_states_cl3_CL;
                break;
            default:
                break;
        }
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.repeatImage, ConstraintSet.START, 0);
        constraintSet.connect(R.id.repeatImage, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        points = getIntent().getIntExtra("points", 0); // KP
        unitedStatesPoints = getIntent().getIntExtra("unitedStatesPoints", 0); // KP
        unitedStatesHasChecked12Trackers = getIntent().getBooleanExtra("unitedStatesHasChecked12Trackers", false);

        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        unitedStatesPoints = prefs.getInt("storedUnitedStatesPoints_level" + String.valueOf(challengeLevel)
                + "_player" + playerString + "_" + syllableGame, 0);
        unitedStatesHasChecked12Trackers = prefs.getBoolean("storedUnitedStatesHasChecked12Trackers_level"
                + challengeLevel + "_player" + playerString + "_" + syllableGame, false);

        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP

        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        int gameID = 0;
        switch (challengeLevel) {
            case 2:
                setContentView(R.layout.united_states_cl2);
                upperTileLimit = 7;
                neutralFontSize = 24;
                gameID = R.id.united_states_cl2_CL;
                break;
            case 3:
                setContentView(R.layout.united_states_cl3);
                upperTileLimit = 9;
                neutralFontSize = 18;
                gameID = R.id.united_states_cl3_CL;
                break;
            default:
                setContentView(R.layout.united_states_cl1);
                upperTileLimit = 5;
                neutralFontSize = 30;
                gameID = R.id.united_states_cl1_CL;
        }

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(unitedStatesPoints));

        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString + syllableGame;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID, 0);
        if (trackerCount >= 12) {
            unitedStatesHasChecked12Trackers = true;
        }

        updateTrackers();

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(gameID);
        }

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }
        playAgain();

    }

    @Override
    public void onBackPressed() {
        // no action
    }

    public void repeatGame(View view) {

        if (!repeatLocked) {
            playAgain();
        }

    }

    public void playAgain() {

        repeatLocked = true;
        selections = new String[]{"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""}; // KP
        int lengthOfLOPWord = Integer.MAX_VALUE;  // KP // Arbitrarily high number to force into the loop

        while (lengthOfLOPWord > upperTileLimit) { // Ensure that the selected word is not too long for a 5/7/9-tile max game
            boolean freshWord = false;

            while (!freshWord) { // Generates a new word if it got one of the last three tested words // LM
                Random rand = new Random();
                int randomNum = rand.nextInt(Start.wordList.size()); // KP

                wordInLWC = Start.wordList.get(randomNum).nationalWord; // KP
                wordInLOP = Start.wordList.get(randomNum).localWord; // KP

                //If this word isn't one of the 3 previously tested words, we're good // LM
                if (!wordInLWC.equals(lastWord)
                        && !wordInLWC.equals(secondToLastWord)
                        && !wordInLWC.equals(thirdToLastWord)) {
                    freshWord = true;
                    thirdToLastWord = secondToLastWord;
                    secondToLastWord = lastWord;
                    lastWord = wordInLWC;
                }

            }

            if (syllableGame.equals("S")) {
                parsedWordArrayFinal = Start.syllableList.parseWordIntoSyllables(wordInLOP); // JP
            } else {
                parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(wordInLOP); // KP
            }
            lengthOfLOPWord = parsedWordArrayFinal.size(); // KP
        }

        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        switch (challengeLevel) {
            case 2:
                visibleTiles = 14;   // RR
                break;
            case 3:
                visibleTiles = 18;   // RR
                break;
            default:
                visibleTiles = 10;   // RR
        }

        int c = 0;      // Iterate through the parsedWordArray2
        int randomNum2;
        int correspondingRow = 0;

        for (int b = 0; b < visibleTiles; b += 2) {

            Button tileButtonA = (Button) findViewById(TILE_BUTTONS[b]);
            Button tileButtonB = (Button) findViewById(TILE_BUTTONS[b + 1]);

            String tileColorStr = COLORS.get((b % 5) / 2);
            int tileColor = Color.parseColor(tileColorStr);
            tileButtonA.setBackgroundColor(tileColor);
            tileButtonB.setBackgroundColor(tileColor);
            tileButtonA.setTextColor(Color.parseColor("#FFFFFF")); // white
            tileButtonB.setTextColor(Color.parseColor("#FFFFFF")); // white

            tileButtonA.setClickable(true);
            tileButtonB.setClickable(true);

            if (c < parsedWordArrayFinal.size()) {

                if ((parsedWordArrayFinal.get(c) == null) || (parsedWordArrayFinal.get(c).isEmpty())) {
                    c = Util.parsedWordTileLength;
                } else {
                    if (syllableGame.equals("S")) {
                        for (int d = 0; d < Start.syllableList.size(); d++) {
                            if (SAD.contains(parsedWordArrayFinal.get(c))) {
                                correspondingRow = tileList.returnPositionInAlphabet(parsedWordArrayFinal.get(c));
                                break;
                            } else if (Start.syllableList.get(d).syllable.equals(parsedWordArrayFinal.get(c))) {
                                correspondingRow = d;
                                break;
                            }
                        }
                    } else {
                        for (int d = 0; d < Start.tileList.size(); d++) {
                            if (Start.tileList.get(d).baseTile.equals(parsedWordArrayFinal.get(c))) {
                                correspondingRow = d;
                                break;
                            }
                        }
                    }
                }

                Random rand = new Random();
                int randomNum = rand.nextInt(2); // Choosing whether correct tile goes above ( =0 ) or below ( =1 )
                randomNum2 = rand.nextInt(Start.ALT_COUNT); // KP // choosing between 2nd and 4th item of game tiles array
                if (randomNum == 0) {
                    tileButtonA.setText(parsedWordArrayFinal.get(c));   // the correct tile
                    if (syllableGame.equals("S") && !SAD.contains(parsedWordArrayFinal.get(c))) {
                        tileButtonB.setText(Start.syllableList.get(correspondingRow).distractors[randomNum2]);   // the (incorrect) suggested alternative
                    } else {
                        tileButtonB.setText(Start.tileList.get(correspondingRow).altTiles[randomNum2]);   // the (incorrect) suggested alternative
                    }
                } else {
                    if (syllableGame.equals("S") && !SAD.contains(parsedWordArrayFinal.get(c))) {
                        tileButtonA.setText(Start.syllableList.get(correspondingRow).distractors[randomNum2]);   // the (incorrect) suggested alternative
                    } else {
                        tileButtonA.setText(Start.tileList.get(correspondingRow).altTiles[randomNum2]);   // the (incorrect) suggested alternative
                    }
                    tileButtonB.setText(parsedWordArrayFinal.get(c));   // the correct tile
                }
                tileButtonA.setVisibility(View.VISIBLE);
                tileButtonB.setVisibility(View.VISIBLE);
            } else {
                tileButtonA.setText("");
                tileButtonB.setText("");
                tileButtonA.setVisibility(View.INVISIBLE);
                tileButtonB.setVisibility(View.INVISIBLE);
            }

            c++;
        }

        TextView constructedWord = findViewById(R.id.activeWordTextView); // KP
        constructedWord.setText(""); // KP

        setAllTilesClickable();

    }

    public void buildWord(String tileNumber, String tileString) {

        TextView constructedWord = findViewById(R.id.activeWordTextView);

        StringBuilder displayedWord = new StringBuilder(); // KP

        for (int j = 0; j < selections.length; j++) {
            if (!selections[j].equals("")) {
                displayedWord.append(selections[j]);
            }
        }

        constructedWord.setText(displayedWord);

        if (displayedWord.toString().equals(Start.wordList.stripInstructionCharacters(wordInLOP))) {

            // Good job!
            repeatLocked = false;
            constructedWord.setTextColor(Color.parseColor("#006400")); // dark green
            constructedWord.setTypeface(constructedWord.getTypeface(), Typeface.BOLD);

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points += 2;
            unitedStatesPoints += 2;
            pointsEarned.setText(String.valueOf(unitedStatesPoints));

            trackerCount++;
            updateTrackers();

            SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.apply();
            editor.putInt("storedUnitedStatesPoints_level" + challengeLevel + "_player"
                    + playerString + "_" + syllableGame, unitedStatesPoints);
            editor.apply();
            editor.putBoolean("storedUnitedStatesHasChecked12Trackers_level" + challengeLevel + "_player"
                    + playerString + "_" + syllableGame, unitedStatesHasChecked12Trackers);
            editor.apply();
            String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString + syllableGame;
            editor.putInt(uniqueGameLevelPlayerID, trackerCount);
            editor.apply(); // requires API 9 as per https://developer.android.com/reference/android/content/SharedPreferences.Editor

            for (int i = 0; i < visibleTiles; i++) {
                TextView gameTile = findViewById(TILE_BUTTONS[i]);
                gameTile.setClickable(false);
            }

            playCorrectSoundThenActiveWordClip(false);

        } else {
            constructedWord.setTextColor(Color.BLACK);
            constructedWord.setTypeface(constructedWord.getTypeface(), Typeface.NORMAL);
        }
    }

    public void onBtnClick(View view) {
        int justClickedTile = Integer.parseInt((String) view.getTag());
        int tileIndex = justClickedTile - 1; //  justClickedTile uses 1 to 10 (or 14 or 18), tileNo uses the array ID (between [0] and [9] (or [13] or [17])
        int otherTileIndex; // the corresponding tile that is above or below the justClickedTile
        if (justClickedTile % 2 == 0) {
            otherTileIndex = tileIndex - 1;
        } else {
            otherTileIndex = tileIndex + 1;
        }

        Button tile = findViewById(TILE_BUTTONS[tileIndex]);
        Button otherTile = findViewById(TILE_BUTTONS[otherTileIndex]);

        String tileColorStr = COLORS.get((tileIndex / 2) % 5);
        int tileColorNo = Color.parseColor(tileColorStr);
        tile.setBackgroundColor(tileColorNo);
        tile.setTextColor(Color.parseColor("#FFFFFF")); // white

        String tileColorStr2 = "#A9A9A9"; // dark gray
        int tileColorNo2 = Color.parseColor(tileColorStr2);
        otherTile.setBackgroundColor(tileColorNo2);
        otherTile.setTextColor(Color.parseColor("#000000")); // black

        selections[tileIndex] = tile.getText().toString();
        selections[otherTileIndex] = "";

        buildWord((String) view.getTag(), selections[tileIndex]);

    }

    protected void setAllTilesUnclickable() {
        for (int t = 0; t < upperTileLimit; t++) {
            TextView gameTile = findViewById(getTileButtons()[t]);
            gameTile.setClickable(false);
        }
    }

    protected void setAllTilesClickable() {

        for (int t = 0; t < upperTileLimit; t++) {
            TextView gameTile = findViewById(getTileButtons()[t]);
            gameTile.setClickable(true);
        }
    }

    public void clickPicHearAudio(View view) {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }

    public void playAudioInstructions(View view) {
        if (getAudioInstructionsResID() > 0) {
            super.playAudioInstructions(view);
        }
    }

}
