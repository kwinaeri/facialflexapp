package com.example.facialflex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreen extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyAppPreferences";
    private static final String KEY_LOGGED_IN = "logged_in";

    private static final int SPLASH_DISPLAY_LENGTH = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Retrieve shared preferences
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_LOGGED_IN, false);

        // Delay for splash screen and navigate based on login status
        new Handler().postDelayed(() -> {
            Intent intent;
            if (isLoggedIn) {
                intent = new Intent(SplashScreen.this, MainActivity.class);
            } else {
                intent = new Intent(SplashScreen.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DISPLAY_LENGTH);
    }
}