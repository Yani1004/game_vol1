package com.example.game_vol1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private Button btnPlay, btnQuests, btnCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // 1. Намираме бутоните от дизайна
        btnPlay = findViewById(R.id.btnPlay);
        btnQuests = findViewById(R.id.btnQuests);
        btnCollection = findViewById(R.id.btnCollection);

        // 2. Действие за големия бутон ИГРАЙ
        btnPlay.setOnClickListener(v -> {
            // Прехвърляме играча към Картата (В момента това е MainActivity)
            Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
            startActivity(intent);
        });


        // 3. Действие за бутона Предизвикателства (Куестове)
        btnQuests.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MainMenuActivity.this, QuestsActivity.class);
            startActivity(intent);
        });

        // 4. Действие за бутона Колекция
        btnCollection.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MainMenuActivity.this, CollectionActivity.class);
            startActivity(intent);
        });
    }
}