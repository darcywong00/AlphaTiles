package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.Random;

import static org.alphatilesapps.alphatiles.Start.*;

public class Myanmar extends GameActivity {

    int clickCount = 0;
    String[][] sevenWordsInLopLwc = new String[7][2];
    String[][] tilesBoard = new String[7][7];
    int tilesInUse;
    int firstClickIndex = 0;
    int secondClickIndex = 0;
    int lowerClick = 0;
    int higherClick = 0;
    int wordsCompleted = 0;
    int completionGoal = 0;
    int myanmarPoints;
    boolean myanmarHasChecked12Trackers;

    protected static final int[] TILE_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16, R.id.tile17, R.id.tile18, R.id.tile19, R.id.tile20,
            R.id.tile21, R.id.tile22, R.id.tile23, R.id.tile24, R.id.tile25, R.id.tile26, R.id.tile27, R.id.tile28, R.id.tile29, R.id.tile30,
            R.id.tile31, R.id.tile32, R.id.tile33, R.id.tile34, R.id.tile35, R.id.tile36, R.id.tile37, R.id.tile38, R.id.tile39, R.id.tile40,
            R.id.tile41, R.id.tile42, R.id.tile43, R.id.tile44, R.id.tile45, R.id.tile46, R.id.tile47, R.id.tile48, R.id.tile49
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
//          audioInstructionsResID = res.getIdentifier("myanmar_" + challengeLevel, "raw", context.getPackageName());
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

        int gameID = R.id.myanmarCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.repeatImage, ConstraintSet.START, 0);
        constraintSet.connect(R.id.repeatImage, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);

    }

    private static final int[] WORD_IMAGES = {
            R.id.wordImage01, R.id.wordImage02, R.id.wordImage03, R.id.wordImage04, R.id.wordImage05, R.id.wordImage06, R.id.wordImage07
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.myanmar);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(R.id.myanmarCL);
        }

        points = getIntent().getIntExtra("points", 0); // KP
        myanmarPoints = getIntent().getIntExtra("myanmarPoints", 0); // LM
        myanmarHasChecked12Trackers = getIntent().getBooleanExtra("myanmarHasChecked12Trackers", false);

        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        myanmarPoints = prefs.getInt("storedMyanmarPoints_level" + challengeLevel + "_player"
                + playerString + "_" + syllableGame, 0);
        myanmarHasChecked12Trackers = prefs.getBoolean("storedMyanmarHasChecked12Trackers_level"
                + challengeLevel + "_player" + playerString + "_" + syllableGame, false);

        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP
        visibleTiles = TILE_BUTTONS.length;

        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(myanmarPoints));

        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString + syllableGame;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID, 0);
        if (trackerCount >= 12) {
            myanmarHasChecked12Trackers = true;
        }

        updateTrackers();

        setTextSizes();

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }

        playAgain();

    }

    @Override
    public void onBackPressed() {
        // no action
    }

    public void setTextSizes() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightOfDisplay = displayMetrics.heightPixels;
        int pixelHeight = 0;
        double scaling = 0.45;
        int bottomToTopId;
        int topToTopId;
        float percentBottomToTop;
        float percentTopToTop;
        float percentHeight;

        for (int t = 0; t < TILE_BUTTONS.length; t++) {

            TextView tile = findViewById(TILE_BUTTONS[t]);
            if (t == 0) {
                ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) tile.getLayoutParams();
                bottomToTopId = lp1.bottomToTop;
                topToTopId = lp1.topToTop;
                percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId).getLayoutParams()).guidePercent;
                percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId).getLayoutParams()).guidePercent;
                percentHeight = percentBottomToTop - percentTopToTop;
                pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
            }
            tile.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

        }

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);
        ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams) wordToBuild.getLayoutParams();
        int bottomToTopId2 = lp2.bottomToTop;
        int topToTopId2 = lp2.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId2).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId2).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
        wordToBuild.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

        // Requires an extra step since the image is anchored to guidelines NOT the textview whose font size we want to edit
        TextView pointsEarned = findViewById(R.id.pointsTextView);
        ImageView pointsEarnedImage = (ImageView) findViewById(R.id.pointsImage);
        ConstraintLayout.LayoutParams lp3 = (ConstraintLayout.LayoutParams) pointsEarnedImage.getLayoutParams();
        int bottomToTopId3 = lp3.bottomToTop;
        int topToTopId3 = lp3.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId3).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId3).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (0.5 * scaling * percentHeight * heightOfDisplay);
        pointsEarned.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

    }

    public void repeatGame(View View) {

        if (!repeatLocked) {
            playAgain();
        }

    }

    public void playAgain() {

        repeatLocked = true;
        wordsCompleted = 0;
        clickCount = 0;
        firstClickIndex = 0;
        secondClickIndex = 0;
        lowerClick = 0;
        higherClick = 0;
        completionGoal = 7;

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);
        wordToBuild.setText("");
        chooseWords();
        resetBoard();
        addWordsToBoard();
        addTilesToRemainingSpaces();
    }

    private void chooseWords() {

        Random rand = new Random();

        for (int i = 0; i < 7; i++) {

            int randomNum = rand.nextInt(Start.wordList.size()); // KP

            sevenWordsInLopLwc[i][0] = Start.wordList.get(randomNum).nationalWord;
            sevenWordsInLopLwc[i][1] = Start.wordList.get(randomNum).localWord;

            int tileLength = 0;

            for (int j = 0; j < i; j++) {
                tileLength = tilesInArray(Start.tileList.parseWordIntoTiles(sevenWordsInLopLwc[i][1]));
                if (sevenWordsInLopLwc[i][0].equals(sevenWordsInLopLwc[j][0])) {
                    i--;
                } else if (tileLength < 3 || tileLength > 7) {
                    i--;
                }
            }

        }

    }

    public void resetBoard() {

        for (int i : TILE_BUTTONS) {
            TextView tile = findViewById(i);
            tile.setText("");
            tile.setBackgroundColor(Color.parseColor("#FFFFFF")); // white
            tile.setTextColor(Color.parseColor("#000000")); // black
        }

        for (int x = 0; x < 7; x++) {

            for (int y = 0; y < 7; y++) {

                tilesBoard[x][y] = "";

            }

        }
    }

    public void addWordsToBoard() {

        for (int w = 0; w < 7; w++) {

            int min = 0;
            int maxXY = 6;
            int maxDirections;
            switch (challengeLevel) {
                case 2:
                    maxDirections = 4; // normal plus diagonal not reverse, from [0] to [4]
                    break;
                case 3:
                    maxDirections = 7; // normal plus diagonal plus reverse (including reverse-diagonal), from [0] to [7]
                    break;
                default:
                    maxDirections = 1;  // normal only (straight right, straight down), from [0] to [1]
            }

            boolean wordFail = false;
            int wordLen = 0;
            // direction is based on a keyboard (e.g. 2 = south, 9 = NE, etc.) value 2 = x-movement, value 3 = y-movement
            int[][] directions = new int[][]{{2, 0, 1}, {6, 1, 0}, {1, -1, 1}, {3, 1, 1}, {9, 1, 0}, {4, -1, 0}, {7, -1, -1}, {8, 0, -1}};
            int wordDirection;
            int loops = 0;
            int leftExitFails = 0;
            int rightExitFails = 0;
            int topExitFails = 0;
            int bottomExitFails = 0;
            int overwriteFails = 0;
            boolean wordPlaced = false;

            Random rand = new Random();

            while (!wordPlaced && loops < 100) {

                loops++;

                int startX = rand.nextInt((maxXY - min) + 1) + min;
                int startY = rand.nextInt((maxXY - min) + 1) + min;
                int wordD = rand.nextInt((maxDirections - min) + 1) + min;

                wordDirection = directions[wordD][0];
                wordFail = false;
                wordLen = tilesInArray(Start.tileList.parseWordIntoTiles(sevenWordsInLopLwc[w][1]));

                // four checks to ensure that the word will not leave the board
                if (wordDirection == 1 || wordDirection == 2 || wordDirection == 3) {

                    if (startY + wordLen > 7) {
                        wordFail = true;
                        bottomExitFails++;
                    }

                }

                if (!wordFail && (wordDirection == 3 || wordDirection == 6 || wordDirection == 9)) {

                    if (startX + wordLen > 7) {
                        wordFail = true;
                        rightExitFails++;
                    }

                }

                if (!wordFail && (wordDirection == 7 || wordDirection == 8 || wordDirection == 9)) {

                    if (startY - wordLen < -1) {
                        wordFail = true;
                        topExitFails++;
                    }

                }

                if (!wordFail && (wordDirection == 1 || wordDirection == 4 || wordDirection == 7)) {

                    if (startX - wordLen < -1) {
                        wordFail = true;
                        leftExitFails++;
                    }

                }

                if (!wordFail) {
                    // check that the intended location of the next word is empty
                    int tileX = 0;
                    int tileY = 0;
                    for (int t = 0; t < wordLen; t++) {

                        tileX = startX + (t * directions[wordD][1]);
                        tileY = startY + (t * directions[wordD][2]);

                        if (!tilesBoard[tileX][tileY].isEmpty()) {

                            wordFail = true;
                            t = wordLen;
                            overwriteFails++;

                        }
                    }
                }

                if (!wordFail) {
                    // add the next word

                    wordPlaced = true;

                    parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(sevenWordsInLopLwc[w][1]);
                    int tileX = 0;
                    int tileY = 0;
                    for (int t = 0; t < wordLen; t++) {

                        tileX = startX + (t * directions[wordD][1]);
                        tileY = startY + (t * directions[wordD][2]);

                        tilesBoard[tileX][tileY] = parsedWordArrayFinal.get(t);

                    }

                    ImageView image = findViewById(WORD_IMAGES[w]);
                    int resID = getResources().getIdentifier(sevenWordsInLopLwc[w][0] + "2", "drawable", getPackageName());
                    image.setImageResource(resID);
                    image.setVisibility(View.VISIBLE);

                }

            }

            if (wordFail) {
                ImageView image = findViewById(WORD_IMAGES[w]);
                image.setImageResource(0);
                image.setVisibility(View.INVISIBLE);
                completionGoal--;
            }

            int tileNumber;
            for (int x = 0; x < 7; x++) {

                for (int y = 0; y < 7; y++) {

                    tileNumber = y * 7 + x;
                    TextView tile = findViewById(TILE_BUTTONS[tileNumber]);
                    tile.setText(tilesBoard[x][y]);

                }

            }
        }
    }

    public void addTilesToRemainingSpaces() {

        String randomTile = "";
        Random rand = new Random();

        int tileNumber;
        for (int x = 0; x < 7; x++) {

            for (int y = 0; y < 7; y++) {

                if (tilesBoard[x][y].isEmpty()) {

                    int randomNum = rand.nextInt(Start.tileList.size()); // KP
                    randomTile = Start.tileList.get(randomNum).baseTile;

                    tilesBoard[x][y] = randomTile;

                    tileNumber = y * 7 + x;
                    TextView tile = findViewById(TILE_BUTTONS[tileNumber]);
                    tile.setText(randomTile);

                }
            }

        }

    }

    private void respondToTileSelection(int justClickedTile) {

        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        TextView tile = findViewById(TILE_BUTTONS[justClickedTile - 1]);

        int textColor = tile.getCurrentTextColor();

        boolean alreadyCompletedTile = false;
        if (textColor == -1) {
            alreadyCompletedTile = true;
            // only the completed tiles switch to white (-1) font
        }

        if (!alreadyCompletedTile) {
            tile.setBackgroundColor(Color.parseColor("#FFEB3B")); // the yellow that the xml design tab suggested
            clickCount++;

            if (clickCount == 1) {

                firstClickIndex = justClickedTile - 1;

            }

            if (clickCount == 2) {

                secondClickIndex = justClickedTile - 1;
                evaluateTwoClicks();

            }
        }

        setAllTilesClickable();
        setOptionsRowClickable();

    }

    private void evaluateTwoClicks() {

        clickCount = 0;

        boolean wordFound = false;

        if (firstClickIndex == secondClickIndex) {

            TextView tileA = findViewById(TILE_BUTTONS[firstClickIndex]);
            tileA.setBackgroundColor(Color.parseColor("#FFFFFF")); // white
            tileA.setTextColor(Color.parseColor("#000000")); // black

            TextView tileB = findViewById(TILE_BUTTONS[secondClickIndex]);
            tileB.setBackgroundColor(Color.parseColor("#FFFFFF")); // white
            tileB.setTextColor(Color.parseColor("#000000")); // black

            setAllTilesClickable();
            setOptionsRowClickable();
            return;

        }

        if (firstClickIndex > secondClickIndex) {

            lowerClick = secondClickIndex;
            higherClick = firstClickIndex;

        } else {

            higherClick = secondClickIndex;
            lowerClick = firstClickIndex;

        }

        int difference = higherClick - lowerClick;

        int selectionDirection = 0;     // 0 = invalid, 46 = horizontal, 82 = vertical, 19 = SW to NE diagonal, 73 = NW to SE diagonal

        if ((lowerClick / 7) == (higherClick / 7)) {

            selectionDirection = 46; // horizontal

        }

        if (difference % 7 == 0) {

            selectionDirection = 82; // vertical

        }

        if (((higherClick / 7) - (lowerClick / 7)) == -1 * ((higherClick % 7) - (lowerClick % 7))) {

            selectionDirection = 19; // SW to NE diagonal

        }

        if (((higherClick / 7) - (lowerClick / 7)) == ((higherClick % 7) - (lowerClick % 7))) {

            selectionDirection = 73; // NW to SE diagonal

        }

        String builtWord1 = "";
        String builtWord2 = "";
        String displayWord = "";

        int[] incrementF = new int[2];
        int[] incrementB = new int[2];
        int selectionLength = 0;

        if (selectionDirection > 0) {

            int tileX1 = firstClickIndex % 7;
            int tileY1 = firstClickIndex / 7;
            int tileX2 = secondClickIndex % 7;
            int tileY2 = secondClickIndex / 7;
            int selectionLengthX = 1 + Math.abs(tileX2 - tileX1);
            int selectionLengthY = 1 + Math.abs(tileY2 - tileY1);
            if (selectionLengthX > selectionLengthY) {
                selectionLength = selectionLengthX;
            } else {
                selectionLength = selectionLengthY;
            }

            // Check forward
            if (selectionDirection == 46) {
                incrementF[0] = 1;
                incrementF[1] = 0;
            }
            if (selectionDirection == 82) {
                incrementF[0] = 0;
                incrementF[1] = 1;
            }
            if (selectionDirection == 19) {
                incrementF[0] = 1;
                incrementF[1] = -1;
            }
            if (selectionDirection == 73) {
                incrementF[0] = 1;
                incrementF[1] = 1;
            }

            int tileX;
            int tileY;

            for (int t = 0; t < selectionLength; t++) {

                if (selectionDirection == 19) { // Direction 19 is special, because the forward direction starts from the higher index
                    tileX = (higherClick % 7) + (t * incrementF[0]);
                    tileY = (higherClick / 7) + (t * incrementF[1]);
                } else {
                    tileX = (lowerClick % 7) + (t * incrementF[0]);
                    tileY = (lowerClick / 7) + (t * incrementF[1]);
                }

                builtWord1 = builtWord1 + tilesBoard[tileX][tileY];

            }

            // Check backwards
            if (selectionDirection == 46) {
                incrementB[0] = -1;
                incrementB[1] = 0;
            }
            if (selectionDirection == 82) {
                incrementB[0] = 0;
                incrementB[1] = -1;
            }
            if (selectionDirection == 19) { // this is the 1 word direction
                incrementB[0] = -1;
                incrementB[1] = 1;
            }
            if (selectionDirection == 73) {
                incrementB[0] = -1;
                incrementB[1] = -1;
            }

            for (int t = 0; t < selectionLength; t++) {

                if (selectionDirection == 19) { // Direction 19 is special, because the backward direction starts from the lower index
                    tileX = (lowerClick % 7) + (t * incrementB[0]);
                    tileY = (lowerClick / 7) + (t * incrementB[1]);
                } else {
                    tileX = (higherClick % 7) + (t * incrementB[0]);
                    tileY = (higherClick / 7) + (t * incrementB[1]);
                }

                builtWord2 = builtWord2 + tilesBoard[tileX][tileY];

            }

            for (int w = 0; w < 7; w++) {

                if (Start.wordList.stripInstructionCharacters(builtWord1).equals(Start.wordList.stripInstructionCharacters(sevenWordsInLopLwc[w][1]))) {
                    wordFound = true;
                    displayWord = builtWord1;
                }
                if (Start.wordList.stripInstructionCharacters(builtWord2).equals(Start.wordList.stripInstructionCharacters(sevenWordsInLopLwc[w][1]))) {
                    wordFound = true;
                    displayWord = builtWord2;
                }

            }

        }

        if (wordFound) {
//            // Word spelled correctly!

            wordsCompleted++;

            TextView activeWord = findViewById(R.id.activeWordTextView);
            activeWord.setText(Start.wordList.stripInstructionCharacters(displayWord));

            int tileX;
            int tileY;

            for (int t = 0; t < selectionLength; t++) {

                if (selectionDirection == 19) {
                    tileX = (higherClick % 7) + (t * incrementF[0]);
                    tileY = (higherClick / 7) + (t * incrementF[1]);
                } else {
                    tileX = (lowerClick % 7) + (t * incrementF[0]);
                    tileY = (lowerClick / 7) + (t * incrementF[1]);
                }

                TextView tile = findViewById(TILE_BUTTONS[tileY * 7 + tileX]);

                String tileColorStr = COLORS.get(wordsCompleted % 5);
                int tileColor = Color.parseColor(tileColorStr);
                tile.setBackgroundColor(tileColor); // theme color
                tile.setTextColor(Color.parseColor("#FFFFFF")); // white

            }

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points += 2;
            myanmarPoints += 2;
            pointsEarned.setText(String.valueOf(myanmarPoints));

            SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.putInt("storedMyanmarPoints_level" + challengeLevel + "_player"
                    + playerString + "_" + syllableGame, myanmarPoints);
            editor.putBoolean("storedMyanmarHasChecked12Trackers_level" + challengeLevel
                    + "_player" + playerString + "_" + syllableGame, myanmarHasChecked12Trackers);
            editor.apply();
            String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString + "_" +
                    syllableGame;
            editor.putInt(uniqueGameLevelPlayerID, trackerCount);
            editor.apply();

            for (int w = 0; w < 7; w++) {

                if (displayWord.equals(wordList.stripInstructionCharacters(sevenWordsInLopLwc[w][1]))) {

                    wordInLWC = sevenWordsInLopLwc[w][0];

                }

            }

            playCorrectSoundThenActiveWordClip(wordsCompleted == completionGoal);

        } else { // word not found

            TextView tileA = findViewById(TILE_BUTTONS[firstClickIndex]);
            tileA.setBackgroundColor(Color.parseColor("#FFFFFF")); // white
            tileA.setTextColor(Color.parseColor("#000000")); // black

            TextView tileB = findViewById(TILE_BUTTONS[secondClickIndex]);
            tileB.setBackgroundColor(Color.parseColor("#FFFFFF")); // white
            tileB.setTextColor(Color.parseColor("#000000")); // black

        }
    }

    public void onBtnClick(View view) {
        respondToTileSelection(Integer.parseInt((String) view.getTag()));
    }

    @Override
    public void clickPicHearAudio(View view) {

        int justClickedImage = Integer.parseInt((String) view.getTag());
        TextView activeWord = findViewById(R.id.activeWordTextView);
        activeWord.setText(wordList.stripInstructionCharacters(sevenWordsInLopLwc[justClickedImage][1]));

        wordInLWC = sevenWordsInLopLwc[justClickedImage][0];
        playActiveWordClip(wordsCompleted == completionGoal);

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
