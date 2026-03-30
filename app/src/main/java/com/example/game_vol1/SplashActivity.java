package com.example.game_vol1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Таймър, който чака 2 секунди и прехвърля към LoginActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Създаваме "намерение" (Intent) да отидем на Login екрана
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);

            // Затваряме Splash екрана, за да не може потребителят да се върне на него с бутона "Назад"
            finish();
        }, 2000); // 2000 милисекунди = 2 секунди
    }
}