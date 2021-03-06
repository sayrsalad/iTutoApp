package com.ituto.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.ituto.android.Services.SocketIOService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            SharedPreferences userPref = getApplicationContext().getSharedPreferences("user",Context.MODE_PRIVATE);
            boolean isLoggedIn = userPref.getBoolean("isLoggedIn",false);

            if (isLoggedIn){
                startActivity(new Intent(MainActivity.this,HomeActivity.class));
                finish();
            } else {
                isFirstTime();
            }
        }, 1500);
    }

    private void isFirstTime() {
        SharedPreferences preferences = getApplication().getSharedPreferences( "onBoard", Context.MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean("isFirstTime", true);
        if (isFirstTime) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();
            startActivity(new Intent( MainActivity.this, OnBoardActivity.class));
        } else {
            startActivity(new Intent( MainActivity.this, AuthActivity.class));
        }
        finish();

    }
}