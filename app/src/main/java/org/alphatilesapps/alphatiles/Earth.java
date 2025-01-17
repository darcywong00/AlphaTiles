package org.alphatilesapps.alphatiles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.DrawableCompat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static org.alphatilesapps.alphatiles.Start.*;


public class Earth extends AppCompatActivity {
    Context context;
    String scriptDirection = Start.langInfoList.find("Script direction (LTR or RTL)");

    int points = 0;

    int challengeLevel;

    int playerNumber = -1;

    int gameNumber;

    String country;

    int pageNumber; // Games 001 to 023 are displayed on page 1, games 024 to 046 are displayed on page 2, etc.

    int doorsPerPage = 23;

    ConstraintLayout earthCL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        playerNumber = getIntent().getIntExtra("playerNumber", -1);
        setContentView(R.layout.earth);
        earthCL = findViewById(R.id.earthCL);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (scriptDirection.equals("RTL")) {
            ImageView goForwardImage = (ImageView) findViewById(R.id.goForward);
            ImageView goBackImage = (ImageView) findViewById(R.id.goBack);
            ImageView activePlayerImage = (ImageView) findViewById(R.id.activePlayerImage);

            goForwardImage.setRotationY(180);
            goBackImage.setRotationY(180);
            activePlayerImage.setRotationY(180);
        }

        setTitle(Start.localAppName);

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        points = prefs.getInt("storedPoints_player" + playerString, 0);

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(points));

        ImageView avatar = findViewById(R.id.activePlayerImage);
        int resID = getResources().getIdentifier(String.valueOf(ChoosePlayer.AVATAR_JPG_IDS[playerNumber - 1]), "drawable", getPackageName());
        avatar.setImageResource(resID);

        String defaultName;
        String playerName;
        String localWordForName = langInfoList.find("NAME in local language");
        if (localWordForName.equals("custom")) {
            defaultName = Start.nameList.get(playerNumber - 1);
        } else {
            defaultName = localWordForName + " " + playerNumber;
        }
        playerName = prefs.getString("storedName" + playerString, defaultName);
        TextView name = findViewById(R.id.avatarName);
        name.setText(playerName);

        pageNumber = getIntent().getIntExtra("pageNumber", 0);

        updateDoors();

        if (scriptDirection.equals("RTL")) {
            forceRTLIfSupported();
        } else {
            forceLTRIfSupported();
        }
    }

    @Override
    public void onBackPressed() {
        // no action
    }

    public void updateDoors() {

        String project = "org.alphatilesapps.alphatiles.";  // how to call this with code? It seemed to produce variable results

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        int trackerCount;

        for (int j = 0; j < earthCL.getChildCount(); j++) {
            View child = earthCL.getChildAt(j);
            if (child instanceof TextView && child.getTag() != null) {
                try {
                    int doorIndex = Integer.parseInt((String) earthCL.getChildAt(j).getTag()) - 1;
                    String doorText = String.valueOf((pageNumber * doorsPerPage) + doorIndex + 1);
                    ((TextView) child).setText(doorText);
                    if (((pageNumber * doorsPerPage) + doorIndex) >= Start.gameList.size()) {
                        ((TextView) child).setVisibility(View.INVISIBLE);
                    } else {
                        country = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).gameCountry;
                        String challengeLevel = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).gameLevel;
                        String syllableGame = gameList.get((pageNumber * doorsPerPage) + doorIndex).gameMode;
                        String uniqueGameLevelPlayerID = String.format("%s%s%s%s%s", project, country, challengeLevel, playerString, syllableGame);

                        trackerCount = prefs.getInt(uniqueGameLevelPlayerID, 0);

                        // This is currently the only game that has no right/wrong responses with an incrementing trackerCount variable
                        // So we are forcing this game's door to initialize with a start
                        // This code is in two places
                        // If other "no right or wrong" games are added, probably better to add a new column in aa_games.txt with a classification
                        if (country.equals("Romania") || country.equals("Sudan")) {
                            trackerCount = 12;
                            ((TextView) child).setTextColor(Color.parseColor("#000000")); // black;
                        } else if (trackerCount < 12) {
                            ((TextView) child).setTextColor(Color.parseColor("#FFFFFF")); // white;
                        } else { // >= 12
                            String textColor = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).gameColor;
                            ((TextView) child).setTextColor(Color.parseColor(COLORS.get(Integer.parseInt(textColor))));
                        }

                        boolean changeColor = true;
                        String doorStyle = "";
                        if (country.equals("Sudan") || country.equals("Romania")) {
                            doorStyle = "_inprocess";
                        } else if (trackerCount > 0 && trackerCount < 12) {
                            doorStyle = "_inprocess";
                        } else if (trackerCount >= 12) {
                            doorStyle = "_mastery";
                            changeColor = false;
                        } else { // 0
                            doorStyle = "";
                        }

                        String drawableBase = "zz_door";

                        String drawableEntryName = drawableBase + doorStyle;

                        int resId = getResources().getIdentifier(drawableEntryName, "drawable", getPackageName());
                        Drawable unwrappedDrawable = AppCompatResources.getDrawable(context, resId);
                        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                        if (changeColor) {
                            DrawableCompat.setTint(wrappedDrawable, Color.parseColor(COLORS.get(
                                    Integer.parseInt(Start.gameList.get((pageNumber * doorsPerPage)
                                            + doorIndex).gameColor))));
                        }
                        ((TextView) child).setBackground(wrappedDrawable);
                        ((TextView) child).setVisibility(View.VISIBLE);

                    }
                } catch (Throwable ex)    // Never reached if tags are well formed!
                {
                    ex.printStackTrace();
                    continue;
                }
            }
        }

        ImageView backArrow = findViewById(R.id.goBack);
        if (pageNumber == 0) {
            backArrow.setVisibility(View.INVISIBLE);
        } else {
            backArrow.setVisibility(View.VISIBLE);
        }

        ImageView forwardArrow = findViewById(R.id.goForward);
        if (((pageNumber + 1) * doorsPerPage) < Start.gameList.size()) {
            forwardArrow.setVisibility(View.VISIBLE);
        } else {
            forwardArrow.setVisibility(View.INVISIBLE);
        }

    }

    public void goToAboutPage(View view) {

        Intent intent = getIntent();
        intent.setClass(context, About.class);
        startActivity(intent);

    }

    public void goBackToChoosePlayer(View view) {

        startActivity(new Intent(context, ChoosePlayer.class));
        finish();

    }

    public void goToResources(View view) {

        Intent intent = getIntent();
        intent.setClass(context, Resources.class);
        startActivity(intent);

    }

    public void goToDoor(View view) {

        finish();
        int doorIndex = Integer.parseInt((String) view.getTag()) - 1;
        String project = "org.alphatilesapps.alphatiles.";  // how to call this with code? It seemed to produce variable results
        String country = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).gameCountry;
        String activityClass = project + country;

        challengeLevel = Integer.parseInt(Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).gameLevel);
        gameNumber = (pageNumber * doorsPerPage) + doorIndex + 1;
        String syllableGame = gameList.get((pageNumber * doorsPerPage) + doorIndex).gameMode;

        Intent intent = getIntent();    // preserve Extras
        try {
            intent.setClass(context, Class.forName(activityClass));    // so we retain the Extras
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        intent.putExtra("challengeLevel", challengeLevel);
        intent.putExtra("points", points);
        intent.putExtra("gameNumber", gameNumber);
        intent.putExtra("pageNumber", pageNumber);
        intent.putExtra("country", country);
        intent.putExtra("syllableGame", syllableGame);
        startActivity(intent);
        finish();

    }

    public void goBackward(View view) {

        if (pageNumber > 0) {
            pageNumber--;
        }
        updateDoors();

    }

    public void goForward(View view) {

        if (((pageNumber + 1) * doorsPerPage) < Start.gameList.size()) {
            pageNumber++;
        }
        updateDoors();

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceLTRIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }
}