package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText edtUserLog, edtPassLog, edtRepassLog;
    Button btnRegister;
    TextView btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtUserLog = findViewById(R.id.username);
        edtPassLog = findViewById(R.id.password);
        edtRepassLog = findViewById(R.id.repassword);
        btnRegister = findViewById(R.id.registerbtn);
        btnLogin = findViewById(R.id.loginbtn);

        dbConnect db = new dbConnect(RegisterActivity.this);
        btnLogin.setOnClickListener(v -> {
            Intent i = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(i);
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String user = edtUserLog.getText().toString().trim();
                String pass = edtPassLog.getText().toString().trim();
                String repass = edtRepassLog.getText().toString().trim();

                if (user.isEmpty() || pass.isEmpty() || repass.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pass.equals(repass)) {
                    edtRepassLog.setError("Passwords don't match");
                    return;
                }

                if (db.userExists(user)) {
                    edtUserLog.setError("Username already taken");
                    return;
                }

                if(pass.length()<10 || !pass.matches(".*\\d.*"))
                {
                    edtPassLog.setError("Password must be at least 10 characters and contain at least 1 number");
                    return;
                }

                // Create and save user to DB
                Users newUser = new Users(user, pass, repass);
                db.addUser(newUser);

                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
