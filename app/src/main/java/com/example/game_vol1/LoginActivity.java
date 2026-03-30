package com.example.game_vol1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Свързваме кода с дизайна
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // 2. Какво се случва, когато натиснем бутона?
        btnLogin.setOnClickListener(v -> {
            // Тук по-късно ще добавим код, който проверява дали паролата е вярна.
            // За сега директно прехвърляме играча към Главното меню:

            Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
            startActivity(intent);

            // Затваряме екрана за логин, за да не може играчът да се върне тук с бутона "Назад"
            finish();
        });
    }
}