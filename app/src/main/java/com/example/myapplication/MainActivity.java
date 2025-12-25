package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText edtUserLog, edtPassLog;
    Button btnLogin;
    TextView btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtUserLog = findViewById(R.id.username);
        edtPassLog = findViewById(R.id.password);
        btnLogin = findViewById(R.id.loginbtn);
        btnRegister = findViewById(R.id.registerbtn);

        btnLogin.setOnClickListener(v -> {
            String user = edtUserLog.getText().toString();
            String pass = edtPassLog.getText().toString();

            dbConnect db = new dbConnect(MainActivity.this);

            if (db.checkLogin(user, pass)) {

                // ✅ SAVE LOGGED-IN USER
                getSharedPreferences("auth", MODE_PRIVATE)
                        .edit()
                        .putString("username", user)
                        .apply();

                Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                finish();
            }
            else {
                edtPassLog.setError("Invalid username or password");
            }
        });

        btnRegister.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(i);
        });
    }
}
