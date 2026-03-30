package com.example.game_vol1;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class QuestsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quests);

        Button btnBack = findViewById(R.id.btnBackFromQuests);
        btnBack.setOnClickListener(v -> finish()); // Затваря екрана и се връща в менюто
    }
}